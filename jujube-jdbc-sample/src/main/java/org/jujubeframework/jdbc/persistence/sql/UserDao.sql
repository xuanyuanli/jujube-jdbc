##pageForUserList
select * from `user` u left join  `department` d on u.department_id=d.id
where 1=1
@if name.notBlank
  and u.name like '%${name}%'
@if age>0 || uid<8
  and u.age > ${age}
@if ids.notNull
  id in (ids.iter(','))
@if nameDesc.notBlank
  order by name desc
limit 10
#empty notEmpty blank notBlank null notNull

##pageForUserList2
select * from `user` u left join  `department` d on u.department_id=d.id
order by u.id desc

