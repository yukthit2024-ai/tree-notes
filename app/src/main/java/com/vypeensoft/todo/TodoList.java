package com.vypeensoft.todo;
import java.util.ArrayList;
import java.util.List;
public class TodoList {
    public String name;
    public List<TodoItem> items = new ArrayList<>();
    public TodoList() {}
    public TodoList(String name) { this.name = name; }
}
