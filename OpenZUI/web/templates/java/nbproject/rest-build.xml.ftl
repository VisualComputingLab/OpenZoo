<?xml version="1.0" encoding="UTF-8"?>
<!--
        *** GENERATED - DO NOT EDIT  ***
        -->
<project name="${ComponentID}-rest-build" basedir=".." xmlns:webproject3="http://www.netbeans.org/ns/web-project/3" xmlns:webproject2="http://www.netbeans.org/ns/web-project/2" xmlns:webproject1="http://www.netbeans.org/ns/web-project/1" xmlns:jaxrs="http://www.netbeans.org/ns/jax-rs/1">
    <target name="-check-trim">
        <condition property="do.trim">
            <and>
                <isset property="client.urlPart"/>
                <length string="${r'${client.urlPart}'}" when="greater" length="0"/>
            </and>
        </condition>
    </target>
    <target name="-trim-url" if="do.trim">
        <pathconvert pathsep="/" property="rest.base.url">
            <propertyset>
                <propertyref name="client.url"/>
            </propertyset>
            <globmapper from="*${r'${client.urlPart}'}" to="*/"/>
        </pathconvert>
    </target>
    <target name="-spare-url" unless="do.trim">
        <property name="rest.base.url" value="${r'${client.url}'}"/>
    </target>
    <target name="test-restbeans" depends="run-deploy,-init-display-browser,-check-trim,-trim-url,-spare-url">
        <replace file="${r'${restbeans.test.file}'}" token="${r'${base.url.token}'}" value="${r'${rest.base.url}'}||${r'${rest.application.path}'}"/>
        <condition property="do.browse-url">
            <istrue value="${r'${display.browser}'}"/>
        </condition>
        <antcall target="browse-url"/>
    </target>
    <target name="browse-url" if="do.browse-url">
        <nbbrowse url="${r'${restbeans.test.url}'}"/>
    </target>
</project>
