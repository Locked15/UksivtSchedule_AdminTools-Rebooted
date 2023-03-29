package model.environment.state

import model.environment.log.LogLevel


class GlobalState(val projectDirectory: String, val resourceProjectPath: List<String>,
                  var logLevel: LogLevel)
