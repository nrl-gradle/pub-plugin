package nrlssc.gradle.tasks

import nrlssc.gradle.extensions.PubConfig
import nrlssc.gradle.extensions.PubExtension
import org.gradle.api.DefaultTask

import java.nio.charset.StandardCharsets

abstract class DockerTask extends DefaultTask {

    List<String> getTags(){
        List<String> tags = new ArrayList<>();

        PubExtension pubExt = project.extensions.getByType(PubExtension.class)

        for(PubConfig pubConfig : pubExt.getPubConfigs())
        {
            for(String repoKey : pubConfig.getDockerRepoKeys())
            {
                String tagRoot = repoKey + '/' + project.group.toString().replaceAll(/\./, /\//) + '/' + project.getName() + ':'
                List<String> tagVers = new ArrayList<>()
                tagVers.add('latest')
                tagVers.add(project.getVersion() + '')
                tagVers.addAll(pubExt.getExtraDockerTagVersions())



                if(pubConfig.username == null || pubConfig.username.length() == 0 ||
                        pubConfig.password == null || pubConfig.password.length() == 0){
                    logger.error("Cannot push to docker registry (" + repoKey + ") without valid credentials")
                    return
                }

                logger.debug('docker login')
                execute("docker login " +
                        "-u ${pubConfig.username} " +
                        "--password-stdin " +
                        "$repoKey", pubConfig.password, "Bad credentials")

                String msg = "Successfully pushed docker images to registry for " + project.getName() + " with tags:\n"
                for(String tagVer : tagVers) {
                    logger.debug('docker push ')
                    execute("docker push $tagRoot$tagVer")
                    msg += "    $tagRoot$tagVer\n"
                }

                println(msg)

            }

        }
    }

    String execute(String cmd, String sendToStdin = null, String errorText = null){
        def env = System.getenv().collect { k, v -> "$k=$v" }

        Process p = cmd.execute(env, project.projectDir)
        if(sendToStdin != null) {
            sendToStdin = sendToStdin + "\n"
            p.getOut().write(sendToStdin.getBytes(StandardCharsets.UTF_8))
            p.getOut().close()
        }
        def b = new StringBuffer()
        p.consumeProcessErrorStream(b)

        String txt = p.text

        if(errorText != null && b.toString().trim().length() > 0 && b.toString().contains(errorText))
        {
            throw new RuntimeException('Error in Docker Publish: ' + b)
        }
        return txt + b.toString().trim()
    }
}
