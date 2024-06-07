// Closure ที่ไม่มีพารามิเตอร์
def simpleClosure = { println "Hello, Groovy!" }

// Closure ที่มีพารามิเตอร์หนึ่งตัว
def greetClosure = { name -> println "Hello, $name!" }

// Closure ที่มีพารามิเตอร์หลายตัว
def addClosure = { a, b -> a + b }

simpleClosure.call()  // แสดงผล: Hello, Groovy!
simpleClosure()       // แสดงผล: Hello, Groovy!

greetClosure.call("Alice")  // แสดงผล: Hello, Alice!
greetClosure("Alice")       // แสดงผล: Hello, Alice!

def result = addClosure(5, 3)
println result  // แสดงผล: 8


