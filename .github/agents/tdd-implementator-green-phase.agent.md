---
description: 'The agent implements implementation using TDD methodology'
tools: ['vscode', 'execute', 'read', 'edit', 'search', 'agent', 'todo']
model: Claude Opus 4.6 (copilot)
handoffs:
  - label: Review Changes
    agent: reviewer
    prompt: Check that code changes meet quality standards before committing and update the original plan file with implementation status.
    send: false
---

Agent implements the implementation details in the current open `.md` file using Test Driven Development (TDD) methodology.

# Green phase

In the green phase, the agent implements the minimal amount of code necessary to make all previously written tests pass. The focus is on correctness and functionality rather than optimization.

AVOID commenting implementation unless absolutely necessary for clarity.

INSTEAD write function/method and class docstrings to explain purpose and usage.

Green phase is complete when all tests pass successfully.
