##pageForUserList
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
<#if notBlank(name)>
  and u.`name` like '%${name}%'
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


##queryAgeCount
select COUNT(*) from `user` u where u.age = ${age} and department_id = ${departmentId}


##queryAgeCount2
select COUNT(*) from `user` u where u.age = ${age}


##queryUserName
select u.`name` from `user` u where u.id = ${id}


##queryUserAge
select u.* from `user` u where u.age = ${age} order by id


##queryUserByDepartmentId
select * from `user` u where department_id = ${departmentId}

##queryIdByDepartmentId
select u.id from `user` u where department_id = ${departmentId}

##queryUserByIds
select * from `user` u where u.id in (${join(ids,',')})

##pageForUserUnionQuery2
select * from `user` u where u.age > ${age}
#jujube-union
select * from `user` u where u.department_id = ${departmentId}


##pageForUserUnionQuery3
select * from `user` u where u.age > 20
#jujube-union
select * from `user` u where u.department_id = 1
#jujube-union
select * from `user` u where u.age = 10

