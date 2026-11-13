package com.examplatform.init;

import com.examplatform.entity.MCQQuestion;
import com.examplatform.repository.MCQQuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MCQInitializer implements CommandLineRunner {
    
    @Autowired
    private MCQQuestionRepository mcqRepository;
    
    @Override
    public void run(String... args) {
        // Only initialize if no questions exist
        if (mcqRepository.count() > 0) {
            return;
        }
        
        // Java MCQ Questions
        createJavaQuestions();
        
        // Python MCQ Questions
        createPythonQuestions();
    }
    
    private void createJavaQuestions() {
        // Core Java - OOP
        mcqRepository.save(new MCQQuestion("JAVA", 1, 
            "Which of the following is not a Java feature?",
            "Object-oriented", "Platform independent", "Use of pointers", "Dynamic",
            "C", "Java does not support pointers explicitly for security reasons"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 2,
            "What is the default value of a boolean variable in Java?",
            "true", "false", "null", "0",
            "B", "Default value of boolean is false"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 3,
            "Which keyword is used to prevent inheritance in Java?",
            "static", "final", "abstract", "private",
            "B", "final class cannot be extended"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 4,
            "What is the size of int in Java?",
            "16 bit", "32 bit", "64 bit", "Depends on platform",
            "B", "int is always 32-bit in Java"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 5,
            "Which collection class allows duplicate elements?",
            "HashSet", "TreeSet", "LinkedHashSet", "ArrayList",
            "D", "ArrayList allows duplicates, Sets do not"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 6,
            "What does JVM stand for?",
            "Java Variable Machine", "Java Virtual Machine", "Java Verified Machine", "Java Version Manager",
            "B", "JVM is Java Virtual Machine that executes bytecode"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 7,
            "Which method is called when an object is created?",
            "main()", "start()", "init()", "Constructor",
            "D", "Constructor is called when object is instantiated"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 8,
            "What is the parent class of all classes in Java?",
            "Object", "Class", "Main", "Super",
            "A", "java.lang.Object is the root class"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 9,
            "Which package contains the Collection framework?",
            "java.util", "java.lang", "java.io", "java.net",
            "A", "Collection framework is in java.util package"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 10,
            "What is the output of: System.out.println(5/2)?",
            "2.5", "2", "2.0", "Compile error",
            "B", "Integer division results in 2"));
        
        // Data Structures - Java
        mcqRepository.save(new MCQQuestion("JAVA", 11,
            "What is the time complexity of ArrayList get() operation?",
            "O(1)", "O(n)", "O(log n)", "O(n log n)",
            "A", "ArrayList provides O(1) random access"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 12,
            "Which data structure uses FIFO?",
            "Stack", "Queue", "Array", "Tree",
            "B", "Queue follows First In First Out"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 13,
            "What is the time complexity of HashMap put() in average case?",
            "O(1)", "O(n)", "O(log n)", "O(n log n)",
            "A", "HashMap provides O(1) average case"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 14,
            "Which sorting algorithm does Collections.sort() use?",
            "QuickSort", "MergeSort", "TimSort", "HeapSort",
            "C", "Collections.sort() uses TimSort (hybrid of merge and insertion sort)"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 15,
            "What is a Red-Black Tree used for in Java?",
            "ArrayList implementation", "TreeMap implementation", "HashMap implementation", "LinkedList implementation",
            "B", "TreeMap uses Red-Black Tree"));
        
        // Algorithms - Java
        mcqRepository.save(new MCQQuestion("JAVA", 16,
            "What is the best case time complexity of Bubble Sort?",
            "O(n)", "O(n^2)", "O(log n)", "O(n log n)",
            "A", "Best case is O(n) when array is already sorted"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 17,
            "Which algorithm uses divide and conquer?",
            "Linear Search", "Binary Search", "Bubble Sort", "Insertion Sort",
            "B", "Binary Search divides the search space in half"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 18,
            "What is the space complexity of Merge Sort?",
            "O(1)", "O(n)", "O(log n)", "O(n log n)",
            "B", "Merge Sort requires O(n) auxiliary space"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 19,
            "Which data structure is used for BFS?",
            "Stack", "Queue", "Priority Queue", "Heap",
            "B", "BFS uses Queue"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 20,
            "What is the time complexity of finding an element in a HashSet?",
            "O(1)", "O(n)", "O(log n)", "O(n log n)",
            "A", "HashSet uses hashing for O(1) lookup"));
        
        // Advanced Java
        mcqRepository.save(new MCQQuestion("JAVA", 21,
            "What is the purpose of the 'synchronized' keyword?",
            "Memory management", "Thread synchronization", "Exception handling", "Garbage collection",
            "B", "synchronized ensures thread safety"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 22,
            "Which interface is used for lambda expressions?",
            "Serializable", "Comparable", "Runnable", "All of the above",
            "D", "Lambda expressions work with functional interfaces"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 23,
            "What does the 'volatile' keyword do?",
            "Makes variable thread-safe", "Prevents caching", "Both A and B", "None",
            "C", "volatile ensures visibility across threads"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 24,
            "Which exception is thrown when dividing by zero?",
            "NullPointerException", "ArithmeticException", "IllegalArgumentException", "RuntimeException",
            "B", "ArithmeticException for divide by zero"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 25,
            "What is the default capacity of ArrayList?",
            "8", "10", "16", "20",
            "B", "Default capacity is 10"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 26,
            "Which design pattern creates objects without exposing creation logic?",
            "Singleton", "Factory", "Observer", "Strategy",
            "B", "Factory pattern encapsulates object creation"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 27,
            "What is the time complexity of adding element at end of ArrayList?",
            "O(1) amortized", "O(n)", "O(log n)", "O(1) always",
            "A", "Amortized O(1) due to resizing"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 28,
            "Which method is used to start a thread?",
            "run()", "start()", "execute()", "begin()",
            "B", "start() creates new thread and calls run()"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 29,
            "What is the load factor of HashMap?",
            "0.5", "0.75", "1.0", "0.5 to 1.0",
            "B", "Default load factor is 0.75"));
        
        mcqRepository.save(new MCQQuestion("JAVA", 30,
            "Which Java 8 feature allows methods with implementation in interfaces?",
            "Lambda", "Stream", "Default methods", "Optional",
            "C", "Default methods have implementation in interfaces"));
    }
    
    private void createPythonQuestions() {
        // Core Python
        mcqRepository.save(new MCQQuestion("PYTHON", 1,
            "What is the output of: print(3 * 'abc')?",
            "abcabcabc", "abc3", "Error", "abc abc abc",
            "A", "String multiplication repeats the string"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 2,
            "Which of these is not a valid Python data type?",
            "list", "dict", "array", "tuple",
            "C", "array is not built-in, needs import"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 3,
            "What is the correct way to create a dictionary?",
            "{}", "[]", "()", "dict[]",
            "A", "{} creates an empty dictionary"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 4,
            "What is the output of: print(type([]))?",
            "<class 'list'>", "<class 'array'>", "<class 'tuple'>", "<class 'dict'>",
            "A", "[] creates a list"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 5,
            "Which method adds an element to a set?",
            "append()", "add()", "push()", "insert()",
            "B", "Sets use add(), lists use append()"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 6,
            "What does the 'pass' statement do?",
            "Raises an error", "Does nothing", "Returns None", "Breaks loop",
            "B", "pass is a null operation"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 7,
            "What is the output of: print(2 ** 3)?",
            "6", "8", "9", "Error",
            "B", "** is exponentiation operator"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 8,
            "Which function returns the length of a list?",
            "size()", "length()", "len()", "count()",
            "C", "len() returns length of any sequence"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 9,
            "What is list comprehension?",
            "A way to create lists", "A sorting algorithm", "A search method", "A data type",
            "A", "List comprehension creates lists concisely"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 10,
            "What is the output of: print('Hello'[1:4])?",
            "ell", "ello", "Hel", "ell ",
            "A", "Slicing from index 1 to 4 (exclusive)"));
        
        // Data Structures - Python
        mcqRepository.save(new MCQQuestion("PYTHON", 11,
            "What is the time complexity of list append()?",
            "O(1) amortized", "O(n)", "O(log n)", "O(1) always",
            "A", "Amortized O(1) due to dynamic array resizing"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 12,
            "Which Python structure is immutable?",
            "list", "dict", "tuple", "set",
            "C", "Tuples are immutable sequences"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 13,
            "What does the 'yield' keyword do?",
            "Returns value", "Creates generator", "Breaks loop", "Raises exception",
            "B", "yield creates a generator function"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 14,
            "What is the default return value of a function without return?",
            "0", "None", "False", "Error",
            "B", "Functions return None by default"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 15,
            "Which sorting algorithm does Python's sort() use?",
            "QuickSort", "MergeSort", "Timsort", "HeapSort",
            "C", "Python uses Timsort"));
        
        // Algorithms - Python
        mcqRepository.save(new MCQQuestion("PYTHON", 16,
            "What is the time complexity of 'in' operator for list?",
            "O(1)", "O(n)", "O(log n)", "O(n log n)",
            "B", "Linear search for list"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 17,
            "What is the time complexity of 'in' operator for set?",
            "O(1) average", "O(n)", "O(log n)", "O(n log n)",
            "A", "Sets use hashing for O(1) lookup"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 18,
            "Which data structure for LIFO?",
            "Queue", "Stack", "Deque", "List",
            "B", "Stack is Last In First Out"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 19,
            "What is the space complexity of recursive factorial?",
            "O(1)", "O(n)", "O(log n)", "O(n!)",
            "B", "Recursion uses O(n) stack space"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 20,
            "Which module provides heap queue algorithm?",
            "heapq", "queue", "collections", "heap",
            "A", "heapq module implements heap queue"));
        
        // Advanced Python
        mcqRepository.save(new MCQQuestion("PYTHON", 21,
            "What is a decorator in Python?",
            "A design pattern", "A function that modifies another function", "A class", "A variable",
            "B", "Decorators wrap/modify functions"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 22,
            "What does @staticmethod do?",
            "Makes method static", "Makes method class method", "Makes method abstract", "Makes method private",
            "A", "@staticmethod creates static method"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 23,
            "What is the Global Interpreter Lock (GIL)?",
            "Allows multiple threads", "Prevents multiple threads from executing Python bytecodes", "A lock on global variables", "None of these",
            "B", "GIL ensures only one thread executes at a time"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 24,
            "Which is used for multithreading in Python?",
            "thread", "threading", "multiprocessing", "Both A and B",
            "B", "threading is the high-level module"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 25,
            "What is the purpose of __init__.py?",
            "Initialize variables", "Mark directory as Python package", "Run on startup", "None",
            "B", "__init__.py makes directory a package"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 26,
            "What does 'with' statement do?",
            "Loop through items", "Context manager", "Exception handling", "Function definition",
            "B", "with is used for context management"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 27,
            "What is the output of: print([i for i in range(5) if i % 2 == 0])?",
            "[0, 2, 4]", "[1, 3, 5]", "[0, 1, 2, 3, 4]", "Error",
            "A", "List comprehension with condition filters evens"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 28,
            "Which method is called when object is created?",
            "__new__", "__init__", "__create__", "__start__",
            "B", "__init__ is the constructor"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 29,
            "What is duck typing?",
            "Type checking", "If it looks like a duck, it's a duck", "Error handling", "None",
            "B", "Duck typing focuses on behavior not type"));
        
        mcqRepository.save(new MCQQuestion("PYTHON", 30,
            "What does *args and **kwargs mean?",
            "Arguments and keyword arguments", "Variable arguments", "Both", "None",
            "C", "*args for tuple, **kwargs for dict"));
    }
}
