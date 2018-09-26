package ${basePackage};

<#list imports as line >
import ${line};
</#list>
import lombok.Data;
import org.jujubeframework.jdbc.support.entity.BaseEntity;

<#if needComment && (classComment!'')?trim != ''>
/**
 * ${classComment}
 *
 * @author generator
 * @since ${.now} 
 */
<#else>
/**
 * @author generator
 */
</#if>
@Data
public class ${className}  implements BaseEntity{

<#list columns as col>
    <#if needComment && (col.comment!'')?trim != ''>
	/**
     * ${col.comment}
     */
    </#if>
	private ${col.type} ${col.field};
</#list>
}
