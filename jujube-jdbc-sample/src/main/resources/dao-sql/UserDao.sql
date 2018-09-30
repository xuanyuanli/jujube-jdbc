##pageForUserList
select u.* from `user` u left join  `department` d on u.department_id=d.id
where 1=1
@if name.notBlank
  and u.name like '%${name}%'
@if age > 0
  and u.age > ${age}
@if ids.notNull
  and u.id in (ids.iter(','))
@if nameDesc.notBlank
  order by u.id asc


##pageForUserListOfOrder
select u.* from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc

