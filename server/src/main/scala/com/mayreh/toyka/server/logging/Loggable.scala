package com.mayreh.toyka.server.logging

import org.slf4j.{Logger, LoggerFactory}

trait Loggable {

  lazy val logger: Logger = LoggerFactory.getLogger(getClass)
}
