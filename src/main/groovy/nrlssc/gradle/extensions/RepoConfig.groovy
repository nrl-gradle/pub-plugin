package nrlssc.gradle.extensions


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
}
