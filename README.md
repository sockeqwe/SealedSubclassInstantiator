# Sealed Subclass Instantiator

This is a simple generator that instantiates an instance of each member of a Kotlin `sealed class` hierarchy.
In other words: for a given `sealed class` it instantiates an instance of each subclass.
The main use case for this is to create sample data for unit tests (uses reflection under the hood).

## Releases
Not public releases available yet.

## Usage

For example let's say you have the following `sealed class` hierarchy:

```kotlin
sealed class Car {
    data class RegularCar(val horsePower: Int, val name: String) : Car()
    object UniqueFerrari : Car()
}
```

Use `instantiateSealedSubclasses()` to get a list of instances of each subclass do:

````kotlin
val cars : List<Car> = instantiateSealedSubclasses(Car::class) 
println(cars) // prints: [RegularCar(horsePower=123, name="random string"), UniqueFerrari]
````

### Motivation and my primary use case for this library
When writing unit tests for a Redux Store I want to test if a particular Action (expressed as a sealed class hierarchy) has no side effect on the State of the Redux Store:

```kotlin
sealed class Action {
    class Action1 : Action()
    class Action2 : Action()
    class Action3 : Action()
}

sealed class State {
    class State1 : State()
    class State2 : State()
}
```

```kotlin
class MyReduxStoreTest {

    @Test
    fun `WHEN State1 AND Action2 is dispached THEN state transitions to State2`(){
        val state = State1()
        val store = MyReduxStore(initialState = state)
        store.dispatch(Action2()) // causes a state transition

        val expectedState = State2()
        assertEquals(expectedState, store.state)
    }
   
    @Test
    fun `WHEN State1 THEN no Action other than Action2 causes a state transition`(){
        val state = State1()
        val store = MyReduxStore(initialState = state)
    
        val actionsThatShouldNotChangeState: List<Action> = 
                instantiateSealedSubclasses(Action::class) // Instantiates Action1, Action2, Action3
                    .filter { action -> action is Action2 }  // filters out Action2 instance
    
        for (action in actionsThatShouldNotChangeState){
            store.dispatch(action) // Dispatching an action may change the state of the store
            assertEquals(state, store.state) // State must not have changed by dispatched action above because only Action2 (which is not part of actionsThatShouldNotChangeState) is allowed to change state while store.state is in a particular State1 
        }
    }
}
```

## Supported features
- Kotlin `object`
- Any class with primitives (int, long, boolean, string, char, float, double, short, byte) or enums as constructor parameters:
  ```kotlin
      data class Foo(val i : Int, val str : String) : Parent()  
  ```
- Any class with other class as constructor parameter that is composed of primitives:
  ```kotlin
        data class SomeData(val i : Int, val str : String)
        data class Foo(data : SomeData) : Parent()  
  ```
- `data class` as well as just regular `class` as long as they have a primary or empty constructor
- `null` as constructor parameter
- default constructor parameters
- `Enums`
  ```kotlin
   enum class AnEnum { A, B, C }
   data class Foo(anEnum : AnEnum) : Parent()
  ```

## Not yet supported features
- `List` (priority 1)
- Other collection types like `Set`, `Map`, `Tree`
- `Array`
- Generics
- Any type that has no public constructor