package play.core.server

import play.core.StaticApplication
import play.api.Mode
import java.io.File
import play.core.ApplicationProvider
import play.api.DefaultApplication
import play.api.Play
import scala.util.Success
import play.docs.BuildDocHandlerFactory
import java.util.jar.JarFile

/**
 * Creates a DefaultApplication in Dev mode wrapped by a Static Application Provider and passes it to NettyServer.
 */
object NettyServerStaticDev {
	
	/**
	 * Call just like NettyServer.main(args).
	 */
	def main(args : Array[String]) {
		args
		.headOption
		.orElse(Option(System.getProperty("user.dir")))
		.map { applicationPath =>
			val applicationFile = new File(applicationPath)
			if (!(applicationFile.exists && applicationFile.isDirectory)) {
				println("Bad application path: " + applicationPath)
			} else {
				play.utils.Threads.withContextClassLoader(this.getClass.getClassLoader) {
					try {
						// Locate play-docs jar file on the class path
						val jarFile = System.getProperty("java.class.path").split(":").map(new java.io.File(_))
										.find(_.getName startsWith "play-docs")
										.map(new JarFile(_))
						val buildDocHandler = jarFile map { BuildDocHandlerFactory.fromJar(_, "play/docs/content") }
						
						val appProvider = new ApplicationProvider {
							
							override def handleWebCommand(request: play.api.mvc.RequestHeader): Option[play.api.mvc.Result] = {
								// if play-docs jar file is on the class path, re-route documentation request to buildDocHandler
								buildDocHandler flatMap { _.maybeHandleDocRequest(request).asInstanceOf[Option[play.api.mvc.Result]] }
							}
							
							val application = new DefaultApplication(
								applicationFile,
								this.getClass.getClassLoader,
								None,
								mode = Mode.Dev
							)
							Play.start(application)
							def get = Success(application)
							def path = applicationFile
						}
						new NettyServer(appProvider,
							port = Option(System.getProperty("http.port")).fold(Option(9000))(p => if (p == "disabled") Option.empty[Int] else Option(Integer.parseInt(p))),
							sslPort = Option(System.getProperty("https.port")).map(Integer.parseInt(_)),
							address = Option(System.getProperty("http.address")).getOrElse("0.0.0.0"),
							mode = appProvider.application.mode//Mode.Dev
						)
					} catch {
						case e: ExceptionInInitializerError => throw e.getCause
					}
				}
			}
		}.getOrElse {
			println("No application path supplied")
		}
	}
}