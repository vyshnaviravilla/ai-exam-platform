package com.examplatform.service;

import com.examplatform.model.CodingQuestion;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class QuestionService {

    public List<CodingQuestion> getAllQuestions() {
        return getJavaQuestions();
    }

    public List<CodingQuestion> getQuestionsByType(String testType) {
        return switch (testType.toUpperCase()) {
            case "JAVA" -> getJavaQuestions();
            case "PYTHON" -> getPythonQuestions();
            default -> getJavaQuestions();
        };
    }

    public List<CodingQuestion> getJavaQuestions() {
        return Arrays.asList(
            CodingQuestion.builder()
                .id(1L)
                .title("Two Sum")
                .difficulty("Medium")
                .language("JAVA")
                .description("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\nYou can return the answer in any order.")
                .examples(Arrays.asList(
                    CodingQuestion.Example.builder()
                        .input("nums = [2,7,11,15], target = 9")
                        .output("[0,1]")
                        .explanation("Because nums[0] + nums[1] == 9, we return [0, 1].")
                        .build(),
                    CodingQuestion.Example.builder()
                        .input("nums = [3,2,4], target = 6")
                        .output("[1,2]")
                        .build()
                ))
                .constraints(Arrays.asList(
                    "2 <= nums.length <= 10^4",
                    "-10^9 <= nums[i] <= 10^9",
                    "-10^9 <= target <= 10^9",
                    "Only one valid answer exists."
                ))
                .starterCode("public class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        // Write your code here\n        \n    }\n}")
                .testCases(Arrays.asList(
                    CodingQuestion.TestCase.builder()
                        .input("[2,7,11,15], 9")
                        .expected("[0,1]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[3,2,4], 6")
                        .expected("[1,2]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[3,3], 6")
                        .expected("[0,1]")
                        .hidden(true)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[1,2,3,4,5], 8")
                        .expected("[2,4]")
                        .hidden(true)
                        .build()
                ))
                .build(),
            
            CodingQuestion.builder()
                .id(2L)
                .title("Reverse Linked List")
                .difficulty("Hard")
                .language("JAVA")
                .description("Given the head of a singly linked list, reverse the list, and return the reversed list.\n\nA linked list can be reversed either iteratively or recursively. Could you implement both?")
                .examples(Arrays.asList(
                    CodingQuestion.Example.builder()
                        .input("head = [1,2,3,4,5]")
                        .output("[5,4,3,2,1]")
                        .build(),
                    CodingQuestion.Example.builder()
                        .input("head = [1,2]")
                        .output("[2,1]")
                        .build()
                ))
                .constraints(Arrays.asList(
                    "The number of nodes in the list is the range [0, 5000]",
                    "-5000 <= Node.val <= 5000"
                ))
                .starterCode("/**\n * Definition for singly-linked list.\n * public class ListNode {\n *     int val;\n *     ListNode next;\n *     ListNode() {}\n *     ListNode(int val) { this.val = val; }\n *     ListNode(int val, ListNode next) { this.val = val; this.next = next; }\n * }\n */\npublic class Solution {\n    public ListNode reverseList(ListNode head) {\n        // Write your code here\n        \n    }\n}")
                .testCases(Arrays.asList(
                    CodingQuestion.TestCase.builder()
                        .input("[1,2,3,4,5]")
                        .expected("[5,4,3,2,1]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[1,2]")
                        .expected("[2,1]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[]")
                        .expected("[]")
                        .hidden(true)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[5]")
                        .expected("[5]")
                        .hidden(true)
                        .build()
                ))
                .build()
        );
    }

    public List<CodingQuestion> getPythonQuestions() {
        return Arrays.asList(
            CodingQuestion.builder()
                .id(101L)
                .title("Two Sum")
                .difficulty("Medium")
                .language("PYTHON")
                .description("Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.\n\nYou may assume that each input would have exactly one solution, and you may not use the same element twice.\n\nYou can return the answer in any order.")
                .examples(Arrays.asList(
                    CodingQuestion.Example.builder()
                        .input("nums = [2,7,11,15], target = 9")
                        .output("[0,1]")
                        .explanation("Because nums[0] + nums[1] == 9, we return [0, 1].")
                        .build(),
                    CodingQuestion.Example.builder()
                        .input("nums = [3,2,4], target = 6")
                        .output("[1,2]")
                        .build()
                ))
                .constraints(Arrays.asList(
                    "2 <= len(nums) <= 10^4",
                    "-10^9 <= nums[i] <= 10^9",
                    "-10^9 <= target <= 10^9",
                    "Only one valid answer exists."
                ))
                .starterCode("class Solution:\n    def twoSum(self, nums: list[int], target: int) -> list[int]:\n        # Write your code here\n        pass")
                .testCases(Arrays.asList(
                    CodingQuestion.TestCase.builder()
                        .input("[2,7,11,15], 9")
                        .expected("[0,1]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[3,2,4], 6")
                        .expected("[1,2]")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("[3,3], 6")
                        .expected("[0,1]")
                        .hidden(true)
                        .build()
                ))
                .build(),
            
            CodingQuestion.builder()
                .id(102L)
                .title("Valid Parentheses")
                .difficulty("Hard")
                .language("PYTHON")
                .description("Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid.\n\nAn input string is valid if:\n1. Open brackets must be closed by the same type of brackets.\n2. Open brackets must be closed in the correct order.\n3. Every close bracket has a corresponding open bracket of the same type.")
                .examples(Arrays.asList(
                    CodingQuestion.Example.builder()
                        .input("\"()\"")
                        .output("true")
                        .build(),
                    CodingQuestion.Example.builder()
                        .input("\"()[]{}\"")
                        .output("true")
                        .build(),
                    CodingQuestion.Example.builder()
                        .input("\"(]\"")
                        .output("false")
                        .build()
                ))
                .constraints(Arrays.asList(
                    "1 <= s.length <= 10^4",
                    "s consists of parentheses only '()[]{}'"
                ))
                .starterCode("class Solution:\n    def isValid(self, s: str) -> bool:\n        # Write your code here\n        pass")
                .testCases(Arrays.asList(
                    CodingQuestion.TestCase.builder()
                        .input("\"()\"")
                        .expected("true")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("\"()[]{}\"")
                        .expected("true")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("\"(]\"")
                        .expected("false")
                        .hidden(false)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("\"([)]\"")
                        .expected("false")
                        .hidden(true)
                        .build(),
                    CodingQuestion.TestCase.builder()
                        .input("\"{[]}\"")
                        .expected("true")
                        .hidden(true)
                        .build()
                ))
                .build()
        );
    }
    
    public CodingQuestion getQuestionById(Long id) {
        List<CodingQuestion> all = getJavaQuestions();
        all.addAll(getPythonQuestions());
        return all.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
