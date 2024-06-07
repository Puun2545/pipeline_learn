class Person {
    String name
    int age
    String word
    def distance

    Person() {
        this.name = 'John'
        this.age = 20
        this.word = 'I love you'
        this.distance = 0
    }

    Person(String name, int age, String word) {
        this.name = name
        this.age = age
        this.word = word
        this.distance = 0
    }

    void sayHi() {
        println "Hi, my name is $name, I'm $age years old, and I want to say $word"
    }

    void walk(int distance) {
        this.distance += distance
        println "I'm walking $distance steps, now I'm at $this.distance"

    }
}

def p1 = new Person('John', 20, 'I love you')
p1.sayHi()
p1.walk(10)
p1.walk(-15)
def p2 = new Person('Jane', 22, 'I hate you')
p2.sayHi()
p2.walk(20)
p2.walk(10)