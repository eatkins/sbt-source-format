package com.swoval.format

import sbt.Keys._
import sbt.ScriptedPlugin.autoImport.scriptedBufferLog
import sbt._
import sbt.plugins.SbtPlugin

object Build {
  val scalaCrossVersions @ Seq(scala210, scala212) = Seq("2.10.7", "2.12.7")

  def baseVersion: String = "0.1.7-SNAPSHOT"

  private def settings(args: Def.Setting[_]*): SettingsDefinition =
    Def.SettingsDefinition.wrapSettingsDefinition(args)

  private def commonSettings: SettingsDefinition =
    settings(
      scalaVersion in ThisBuild := scala212,
      organization := "com.swoval",
      homepage := Some(url("https://github.com/swoval/sbt-source-format")),
      scmInfo := Some(
        ScmInfo(url("https://github.com/swoval/sbt-source-format"),
                "git@github.com:swoval/sbt-source-format.git")),
      developers := List(
        Developer("username",
                  "Ethan Atkins",
                  "ethan.atkins@gmail.com",
                  url("https://github.com/eatkins"))),
      licenses += ("MIT", url("https://opensource.org/licenses/MIT")),
      scalacOptions ++= Seq("-feature"),
      publishTo := {
        val p = publishTo.value
        if (sys.props.get("SonatypeSnapshot").fold(false)(_ == "true"))
          Some(Opts.resolver.sonatypeSnapshots): Option[Resolver]
        else if (sys.props.get("SonatypeRelease").fold(false)(_ == "true"))
          Some(Opts.resolver.sonatypeReleases): Option[Resolver]
        else p
      },
      version in ThisBuild := {
        val v = baseVersion
        if (sys.props.get("SonatypeSnapshot").fold(false)(_ == "true")) {
          if (v.endsWith("-SNAPSHOT")) v else s"$v-SNAPSHOT"
        } else {
          v
        }
      },
    )

  lazy val `source-format` = project
    .in(file("."))
    .enablePlugins(SbtPlugin)
    .settings(
      commonSettings,
      scriptedBufferLog := false,
      crossScalaVersions := scalaCrossVersions,
      libraryDependencies += "com.google.googlejavaformat" % "google-java-format" % "1.6",
      sbtVersion in pluginCrossBuild := {
        if ((scalaVersion in crossVersion).value == scala210) "0.13.16" else "1.0.4"
      },
      skip in publish :=
        !version.value.endsWith("-SNAPSHOT") || !sys.props.get("SonatypeSnapshot").fold(true)(_ == "true"),
      crossSbtVersions := Seq("1.1.1", "0.13.17"),
      crossScalaVersions := Seq(scala210, scala212),
      name := "sbt-source-format",
      description := "Format source files using clang-format and the google java format library.",
    )
}
