package org.telegram

<#if fields?has_content>data class<#else>interface</#if> ${title}(
<#list fields as field>
        /**
        * ${field.description}
        */
        val ${field.field}: ${field.type?no_esc}<#if field?has_next>,
</#if>

</#list>
)<#if parent??>: ${parent}()</#if>