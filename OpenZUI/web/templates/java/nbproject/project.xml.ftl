<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://www.netbeans.org/ns/project/1">
    <type>org.netbeans.modules.web.project</type>
    <configuration>
        <buildExtensions xmlns="http://www.netbeans.org/ns/ant-build-extender/1">
            <extension file="rest-build.xml" id="rest.5"/>
        </buildExtensions>
        <data xmlns="http://www.netbeans.org/ns/web-project/3">
            <name>${ComponentID}</name>
            <minimum-ant-version>1.6.5</minimum-ant-version>
            <web-module-libraries>
                <library dirs="200">
                    <file>${r'${libs.restapi.classpath}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${libs.restlib.classpath}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${reference.OpenZooService.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.commons-codec-1.8.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.commons-pool2-2.4.2.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.jettison-1.3.2.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.log4j-api-2.0.2.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.log4j-core-2.0.2.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.rabbitmq-client.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
                <library dirs="200">
                    <file>${r'${file.reference.mongo-2.9.3.jar}'}</file>
                    <path-in-war>WEB-INF/lib</path-in-war>
                </library>
            </web-module-libraries>
            <web-module-additional-libraries/>
            <source-roots>
                <root id="src.dir"/>
            </source-roots>
            <test-roots>
                <root id="test.src.dir"/>
            </test-roots>
        </data>
        <references xmlns="http://www.netbeans.org/ns/ant-project-references/1">
            <reference>
                <foreign-project>OpenZooService</foreign-project>
                <artifact-type>jar</artifact-type>
                <script>build.xml</script>
                <target>jar</target>
                <clean-target>clean</clean-target>
                <id>jar</id>
            </reference>
        </references>
    </configuration>
</project>
