package org.telegram

data class ${title}(
    <#list fields as field>
        /**
        * ${field.description}
        */
        val ${field.field}: ${field.type?no_esc}<#if field?has_next>,
        </#if>

    </#list>
)