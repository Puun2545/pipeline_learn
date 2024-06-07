def a = 10
String a1 = '10'
int a2 = 10

// Check dataType
println "a " + a.getClass()
println "a1 " + a1.getClass()
println "a2 " + a2.getClass()

// Check value

int b1 = 10
println "b1 " + b1.getClass()

def sum1 = a1 + b1
println "Sum 1 : $sum1"
// Output: 1010 because a1 is a string

def sum2 = a2 + b1
println "Sum 2 : $sum2"
// Output: 20 because a2 is an integer + integer

def sumdef = a + b1
println "Sumdef : $sumdef"
// Output: 20 because a is a def + integer -> def can be any type

String sumgg = a + b1
println "Decla Str :" + sumgg + " " + sumgg.getClass()

// Check type casting
def sumcast = a1 as int + b1
println "Cast Str " + sumcast
// Output: 20 because a1 is casted to int before adding

bool1 = (a == a1)
bool2 = (a == a2)
bool3 = (a1 == a2)

println "Bool 1 : $bool1"
println "Bool 2 : $bool2"
println "Bool 3 : $bool3"

