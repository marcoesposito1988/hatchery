package it.achdjian.plugin.ros.settings

import com.intellij.openapi.diagnostic.Logger
import it.achdjian.plugin.ros.utils.getEnvironment
import java.io.File
import java.nio.file.Path

val logger = Logger.getInstance("#it.achdjian.plugin.ros.settings.RosSettingsUtils.findInitCmd")

fun findInitCmd(path: String) = path.split(":")
  .map {
    File(it, "catkin_init_workspace").also {
      logger.trace("Search for ${it.absolutePath}:  exists-> ${it.exists()}")
    }
  }
  .firstOrNull { it.exists() }?.let { InitWorkspaceCmd(it, "") }

fun diffEnvironment(rosVersion: Path): Map<String, String> {
  val log = Logger.getInstance("#it.achdjian.plugin.ros.settings.RosSettingsUtils.diffEnvironment")

  val actualEnv = System.getenv()

  val newEnv = getEnvironment(rosVersion, rosVersion.toAbsolutePath().toString() + "/setup.bash")
  val env = HashMap(diff(newEnv, actualEnv))
  log.trace("Diff env:")
  env.forEach { (key, value) -> log.trace("$key=$value") }
  if (!env.containsKey("ROS_PACKAGE_PATH"))
    env["ROS_PACKAGE_PATH"] = actualEnv["ROS_PACKAGE_PATH"]
  env["PATH"] = newEnv["PATH"]
  return env
}

fun diff(newEnv: Map<String, String>, actualEnv: Map<String, String>) =
  newEnv.filter { (key, value) ->
    !actualEnv.containsKey(key) || !actualEnv[key].equals(value)
  }