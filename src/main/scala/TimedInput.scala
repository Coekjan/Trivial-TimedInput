import TimedInput.{MAX_TIME, TimedMessageFormatError, timedMessagePattern}

import java.io._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class TimedInput(private val inputStream: InputStream) extends Runnable {
  private val pipeIn: PipedInputStream = new PipedInputStream()
  private val pipeOut: PipedOutputStream = new PipedOutputStream()
  try pipeOut.connect(pipeIn) catch {
    case e: IOException => e.printStackTrace()
  }
  new Thread(this).start()

  def getInputStream: InputStream = pipeIn

  override def run(): Unit = {
    val startTime = System.currentTimeMillis()
    val reader = new BufferedReader(new InputStreamReader(inputStream))
    Iterator.continually {
      reader.readLine()
    }.takeWhile(Option(_).nonEmpty).map { line =>
      val (millis, message) = line match {
        case timedMessagePattern(seconds, message) => ((seconds.toDouble * 1000).toLong, message)
        case _ => throw TimedMessageFormatError(line)
      }
      Future {
        val currentTime = System.currentTimeMillis()
        Thread.sleep(millis - (currentTime - startTime))
        try {
          pipeOut.write((message + "\n").getBytes())
          pipeOut.flush()
        } catch {
          case e: IOException => e.printStackTrace()
        }
      }
    }.foreach(Await.ready(_, MAX_TIME))
    try pipeOut.close() catch {
      case e: IOException => e.printStackTrace()
    }
  }
}

object TimedInput {
  private val MAX_TIME = 240 seconds
  private val timedMessagePattern = """^\[(\d+(?:\.\d+)?)](.*)$""".r

  case class TimedMessageFormatError(content: String) extends Exception {
    override def toString: String = s"Bad content: $content"
  }

  def main(args: Array[String]): Unit = {
    val in = new TimedInput(System.in).getInputStream
    val reader = new BufferedReader(new InputStreamReader(in))
    Iterator.continually {
      reader.readLine()
    }.takeWhile(Option(_).nonEmpty).foreach(println)
  }
}
