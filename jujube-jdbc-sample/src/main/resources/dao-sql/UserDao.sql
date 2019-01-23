##pageForUserList
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
<#if notBlank(name)>
  and u.name like '%${name}%'
</#if>
<#if age gt 0>
  and u.age > ${age}
</#if>
<#if notNull(ids)>
  and u.id in (${join(ids,',')})
</#if>
<#if notBlank(nameDesc)>
  order by u.id asc
</#if>


##pageForUserListOfOrder
select u.* from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc

