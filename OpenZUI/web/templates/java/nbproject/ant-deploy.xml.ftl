<?xml version="1.0" encoding="UTF-8"?>
<project default="-deploy-ant" basedir=".">
    <target name="-init" if="deploy.ant.enabled">
        <property file="${r'${deploy.ant.properties.file}'}"/>
        <tempfile property="temp.module.folder" prefix="tomcat" destdir="${r'${java.io.tmpdir}'}"/>
        <unwar src="${r'${deploy.ant.archive}'}" dest="${r'${temp.module.folder}'}">
            <patternset includes="META-INF/context.xml"/>
        </unwar>
        <xmlproperty file="${r'${temp.module.folder}'}/META-INF/context.xml"/>
        <delete dir="${r'${temp.module.folder}'}"/>
    </target>
    <target name="-check-credentials" if="deploy.ant.enabled" depends="-init">
        <fail message="Tomcat password has to be passed as tomcat.password property.">
            <condition>
                <not>
                    <isset property="tomcat.password"/>
                </not>
            </condition>
        </fail>    
    </target>
    <target name="-deploy-ant" if="deploy.ant.enabled" depends="-init,-check-credentials">
        <echo message="Deploying ${r'${deploy.ant.archive}'} to ${r'${Context(path)}'}"/>
        <taskdef name="deploy" classname="org.apache.catalina.ant.DeployTask"
                 classpath="${r'${tomcat.home}'}/server/lib/catalina-ant.jar"/>
        <deploy url="${r'${tomcat.url}'}/manager" username="${r'${tomcat.username}'}"
                password="${r'${tomcat.password}'}" path="${r'${Context(path)}'}"
                war="${r'${deploy.ant.archive}'}"/>
        <property name="deploy.ant.client.url" value="${r'${tomcat.url}'}${r'${Context(path)}'}"/>
    </target>
    <target name="-undeploy-ant" if="deploy.ant.enabled" depends="-init,-check-credentials">
        <echo message="Undeploying ${r'${Context(path)}'}"/>
        <taskdef name="undeploy"  classname="org.apache.catalina.ant.UndeployTask"
                classpath="${r'${tomcat.home}'}/server/lib/catalina-ant.jar"/>
        <undeploy url="${r'${tomcat.url}'}/manager" username="${r'${tomcat.username}'}" 
                  password="${r'${tomcat.password}'}" path="${r'${Context(path)}'}"/>
    </target>
</project>
