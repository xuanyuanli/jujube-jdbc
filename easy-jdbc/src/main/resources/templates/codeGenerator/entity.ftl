package ${basePackage}.entity;

<#if isCache>
import javax.persistence.Cacheable;
</#if>
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

<#if isCache>
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
</#if>
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.SelectBeforeUpdate;

<#list imports as line >
import ${line};
</#list>
import org.dazao.easyjdbc.entity.BaseEntity;

@Entity
@Table(name = "${tableName}", schema = "${schemaName}")
@DynamicUpdate(true)
@SelectBeforeUpdate(true)
<#if isCache>
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
</#if>
<#if needComment && (classComment!'')?trim != ''>
/**
 * ${classComment} 
 *
 * @since ${.now} 
 */
</#if>
public class ${className} extends BaseEntity {

<#list columns as col>
	private ${col.type} ${col.field};
</#list>
<#list columns as col>

	<#if needComment && (col.comment!'')?trim != ''>
	/**
     * ${col.comment}
     */
	</#if>
	<#if col.isPrimaryKey>
	@Id
    @GeneratedValue
    @Column(name = "${col.colName}", unique = true, nullable = false)
    <#else>
    @Column(name = "${col.colName}")
	</#if>
	public ${col.type} get${col.field?cap_first}() {
        return this.${col.field};
    }

    public void set${col.field?cap_first}(${col.type} ${col.field}) {
        this.${col.field} = ${col.field};
    }
</#list>
}
