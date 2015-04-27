package play.core.server

import play.core.StaticApplication
import play.api.Mode
import java.io.File
import play.core.ApplicationProvider
import play.api.DefaultApplication
import play.api.Play
import scala.util.Success

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
						val appProvider = new ApplicationProvider {
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