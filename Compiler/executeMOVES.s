L8

move $t2, $ra

move $$t2, $$t2

sw $$t2, -4($$fp) 

move $s0, $s0

move $s1, $s1

move $s2, $s2

move $s3, $s3

move $s4, $s4

move $s5, $s5

move $s6, $s6

move $s7, $s7

move $fp, $fp

move $t2, $v0

move $t2, $a0

li $t2, 1

move $t2, $t2

li $t4, 1

cjump $t7 < $t4 L2 L1

L2

li $t2, 0

move $t2, $t2

L1

li $t4, 1

cjump $t2 == $t4 L5 L4

L5

move $t7, $t7

move $$t7, $$t7

sw $$t7, 0($$fp) 

	.text
Fac$ComputeFac:
Fac$ComputeFac_framesize=0
call $k1

#	return
	j $ra
move $k1, $k1

lw $$t7, 0($$fp)

move $$t7, $$t7

mul $t7,$t7,$k1

move $t7, $t7

L6

move $fp, $fp

move $s7, $s7

move $s6, $s6

move $s5, $s5

move $s4, $s4

move $s3, $s3

move $s2, $s2

move $s1, $s1

move $s0, $s0

lw $$t2, -4($$fp)

move $$t2, $$t2

move $ra, $t2

move $v0, $t7

j L7 

L4

li $t7, 1

move $t7, $t7

j L6 

L7

