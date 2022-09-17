package nrlssc.gradle.extensions

import org.gradle.util.Configurable


class RepoConfig {

    private static Closure defaultLayout = {
        ivy "[organisation]/[module]/[revision]/ivys/ivy-[revision].xml"
        artifact "[organisation]/[module]/[revision]/[type]s/[artifact]-[revision](-[classifier]).[ext]"
    }
    
    String key
    boolean maven = false
    Closure patternLayout = defaultLayout

    RepoConfig(){

    }
    
    RepoConfig(String key){
        this.key = key
    }
    
    RepoConfig(String key, boolean maven){
        this.key = key
        this.maven = maven
    }
    
    RepoConfig(String key, Closure ivyConf) {
        this.key = key
        this.patternLayout = ivyConf
    }

    String getKey() {
        return key
    }

    void setKey(String key) {
        this.key = key
    }

    boolean getMaven() {
        return maven
    }

    void setMaven(boolean maven) {
        this.maven = maven
    }

    Closure getPatternLayout() {
        return patternLayout
    }

    void setPatternLayout(Closure patternLayout) {
        this.patternLayout = patternLayout
    }

}
