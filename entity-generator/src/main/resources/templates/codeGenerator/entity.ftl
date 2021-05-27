package ${entityPackage};

import lombok.Data;
import lombok.experimental.Accessors;
import org.jujubeframework.jdbc.support.annotation.Column;

<#list imports as line >
import ${line};
</#list>
import org.jujubeframework.jdbc.support.entity.BaseEntity;

<#if needComment && (classComment!'')?trim != ''>
/**
 * ${classComment} 
 *
 * @since ${.now}
 * @author generator
 */
    <#else>
/**
* @author generator
*/
</#if>
@Data
@Accessors(chain = true)
public class ${className} implements BaseEntity {

<#list columns as col>
    <#if needComment && (col.comment!'')?trim != ''>
	/**
     * ${col.comment}
     */
    </#if>
    <#if isAddColumnAnnotation>
    @Column("${col.colName}")
    </#if>
	private ${col.type} ${col.field};
</#list>

}
