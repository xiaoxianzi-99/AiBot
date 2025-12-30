# SQLite Conversation History Feature

## Overview

This document describes the SQLite-based conversation history feature added to AiBot.

## Features

### 1. Persistent Conversation Storage
- All chat conversations are stored in a local SQLite database (`aibot.db`)
- Each conversation has:
  - Unique ID
  - Title (auto-generated from first message)
  - Created timestamp
  - Updated timestamp

### 2. Message Persistence
- All messages (user, AI assistant, and system) are saved to the database
- Each message includes:
  - Conversation ID reference
  - Role (user/assistant/system)
  - Content
  - Timestamp

### 3. Left Sidebar UI
- **Conversation List**: Shows all past conversations sorted by most recent
- **New Conversation Button**: Creates a fresh conversation
- **Conversation Selection**: Click any conversation to load its full history
- **Visual Indicators**: 
  - Title shows preview of conversation topic
  - Timestamp shows last update time

## Database Schema

### Conversations Table
```sql
CREATE TABLE conversations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
)
```

### Messages Table
```sql
CREATE TABLE messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    conversation_id INTEGER NOT NULL,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
)
```

## User Workflow

1. **Starting the App**
   - On first launch, a new conversation is automatically created
   - The left sidebar shows the conversation list

2. **Sending Messages**
   - Type a message and press Enter or click "发送"
   - The message is instantly saved to the database
   - AI response is also saved when received
   - If it's the first message in a conversation, the title is auto-generated

3. **Managing Conversations**
   - Click "**+ 新建对话**" to start a new conversation
   - Click any conversation in the list to switch to it
   - All messages from that conversation will be loaded and displayed

4. **File Uploads**
   - File uploads are also saved to the current conversation
   - Both the upload action and AI's analysis are persisted

## Implementation Details

### Key Components

1. **DatabaseService** (`com.pei.service.DatabaseService`)
   - Singleton service managing all SQLite operations
   - Handles database initialization, CRUD operations
   - Thread-safe database access

2. **Conversation Model** (`com.pei.model.Conversation`)
   - Represents conversation metadata
   - Includes title, timestamps, and ID

3. **Enhanced Message Model** (`com.pei.model.Message`)
   - Extended to include conversation_id and role
   - Supports both in-memory and database-backed messages

4. **ChatController Updates**
   - Integrated DatabaseService
   - Added conversation list management
   - Automatic message persistence on send/receive

### Database Location

The SQLite database file `aibot.db` is created in the application's working directory. This file contains all conversation history and should be backed up to preserve chat history.

### .gitignore

Database files (`*.db`, `*.db-journal`) are excluded from version control to prevent accidental commits of user data.

## Future Enhancements (Not Implemented)

Potential features for future development:
- Delete conversations
- Rename conversations
- Search/filter conversations
- Export conversation history
- Conversation tags/categories
- Multi-user support with separate databases

## Technical Notes

- **Thread Safety**: All database operations are performed in background threads to avoid blocking the UI
- **Auto-refresh**: Conversation list automatically refreshes when conversations are updated
- **Markdown Support**: AI responses continue to support Markdown rendering
- **Backward Compatible**: Existing chat functionality remains unchanged
