package com.example.sprintplanning.model;

import jakarta.xml.bind.annotation.*;

import java.util.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Task {

    @XmlAttribute
    private String text;

    @XmlAttribute
    private boolean done;

    @XmlAttribute
    private boolean important;

    @XmlAttribute
    private boolean urgent;

    @XmlAttribute
    private boolean open;

    @XmlElement(name = "task")
    private List<Task> children;

    // Default constructor required by JAXB
    public Task() {
        this(null);
    }

    public Task(String text) {
        this(text, new ArrayList<>());
    }

    public Task(String text, List<Task> children) {
        this.text = text;
        this.children = children;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public boolean isImportant() { return important; }
    public void setImportant(boolean important) { this.important = important; }

    public boolean isUrgent() { return urgent; }
    public void setUrgent(boolean urgent) { this.urgent = urgent; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    public List<Task> getChildren() { return children; }
    public void setChildren(List<Task> children) { this.children = children; }

    @Override
    public String toString() {
        return text;
    }

}
