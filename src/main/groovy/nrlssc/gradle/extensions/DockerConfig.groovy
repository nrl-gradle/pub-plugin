package nrlssc.gradle.extensions


import org.gradle.api.credentials.Credentials
import org.gradle.util.internal.ConfigureUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DockerConfig {
    private static Logger logger = LoggerFactory.getLogger(DockerConfig.class)

    String dockerfile
    String appendix
    String target

    String imageNameOverride


    DockerConfig()
    {
        this.dockerfile = "Dockerfile"
    }

}
