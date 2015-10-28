package fhj.swengb.exercises.ex02

import java.io.File
import java.net.URL

import fhj.swengb.{Student, Students, SwengbUtil}

import scala.io.Source
import scala.util.Try


object Assessment {

  def main(args: Array[String]): Unit = {
    val assessments =
      Students.sortedStudents.map(s => Assessment("tutorial", s))

    assessments.foreach(_.generateSources())

  }

}

/**
  * The goal of this exercise is to make use of the datamodel we introduced so far.
  */
case class Assessment(name: String, student: Student) {

  /**
    * the Name with uppercase first letter
    */
  val Name: String = name(0).toUpper + name.drop(1)

  /**
    * the project name
    */
  val assignmentName: String = "fhj.swengb.assignments." + name

  /**
    * We create a function to generate a report in the Markdown Format
    * about the status of the tutorial assignment.
    *
    * First, we need a function to fetch data from the Internet. It
    * should handle error cases too.
    *
    * It returns a string representation of the result.
    */
  def fetchFromInternet(url: URL): Try[String] = Try(SwengbUtil.fetch(url))

  /**
    * the url for the assignment
    */
  val assignmentUrl: URL = new URL(student.gitHubHome + assignmentName)

  private val rawBase: String = s"https://raw.githubusercontent.com/${student.githubUsername}/$assignmentName/master"

  /**
    * The URL for the assignment implementation
    */
  val assignmentClazzURL: URL = new URL(s"$rawBase/src/main/scala/fhj/swengb/assignments/$name/${student.userId}/${Name}Assignment.scala")

  /**
    * The URL for the assignment test
    */
  val assignmentTestClazzURL: URL = new URL(s"$rawBase/src/test/scala/fhj/swengb/assignments/$name/${student.userId}/${Name}AssignmentTest.scala")

  /**
    * fetch the main page for this github repository, assert that the page contains a certain
    * string, the project name.
    */
  lazy val gitHubRepoExists = fetchFromInternet(assignmentUrl).map(_.contains(assignmentName)).getOrElse(false)

  def fetchContent(): Try[(String, String)] = {
    for {implSrc <- fetchFromInternet(assignmentClazzURL)
         testSrc <- fetchFromInternet(assignmentTestClazzURL)}
      yield (implSrc, testSrc)
  }

  /**
    * This method call fetches the source code of the student to the workspace.
    */
  def generateSources(): Unit = {

    val targetImpl = new File(s"fhj.swengb.lecturer/students/src/main/scala/fhj/swengb/assignments/$name/${student.userId}/${Name}Assignment.scala")
    val targetTest = new File(s"fhj.swengb.lecturer/students/src/test/scala/fhj/swengb/assignments/$name}/${student.userId}/${Name}AssignmentTest.scala")
    val referenceTest = new File(s"fhj.swengb.assignments.tutorial/src/test/scala/fhj/swengb/assignments/tutorial/rladstaetter/${Name}AssignmentTest.scala")

    def mkLocalEnvironment(): Unit = {
      if (!targetImpl.getParentFile.exists()) {
        targetImpl.getParentFile.mkdirs()
      }
      if (!targetTest.getParentFile.exists()) {
        targetTest.getParentFile.mkdirs()
      }
    }

    val content = Source.fromFile(referenceTest).mkString
    SwengbUtil.writeToFile(targetTest, content.replaceAll("rladstaetter", student.userId))

    for ((implSrc, testSrc) <- fetchContent()) {
      SwengbUtil.writeToFile(targetImpl, implSrc)
      //SwengbUtil.writeToFile(targetTest,testSrc)
    }

  }


}
