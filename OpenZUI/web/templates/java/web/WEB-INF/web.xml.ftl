<?xml version="1.0" encoding="UTF-8"?>
<web-app version="3.0" xmlns="http://java.sun.com/xml/ns/javaee" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">
    <servlet>
        <servlet-name>ServletAdaptor</servlet-name>
        <servlet-class>com.sun.jersey.spi.container.servlet.ServletContainer</servlet-class>
        <init-param>
            <description>Multiple packages, separated by semicolon(;), can be specified in param-value</description>
            <param-name>com.sun.jersey.config.property.packages</param-name>
            <param-value>gr.iti.openzoo.service.rest</param-value>
        </init-param>
        <init-param>
            <param-name>com.sun.jersey.api.json.POJOMappingFeature</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>com.sun.jersey.spi.container.ContainerResponseFilters</param-name>
            <param-value>gr.iti.openzoo.service.impl.${ComponentID}CrossOriginResourceSharingFilter</param-value>
        </init-param>
        <init-param>
            <param-name>com.sun.jersey.config.feature.DisableWADL</param-name>
            <param-value>true</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>ServletAdaptor</servlet-name>
        <url-pattern>/resources/*</url-pattern>
    </servlet-mapping>
    <session-config>
        <session-timeout>
            30
        </session-timeout>
    </session-config>
</web-app>
