# Comment Reply Feature Implementation

## What Was Added

I've implemented a complete comment reply system for the Community feed. Users can now reply to comments, creating threaded conversations.

## Database Migration Required

**IMPORTANT:** Before running the application, you MUST run the SQL migration to add the parent comment field to the database.

### Steps:

1. Open your MySQL client (phpMyAdmin, MySQL Workbench, or command line)
2. Run the SQL file: `add_comment_replies.sql`

Or manually execute this SQL:

```sql
USE `skillora`;

ALTER TABLE `commentaire` 
ADD COLUMN `id_commentaire_parent` INT(11) DEFAULT NULL AFTER `id_post`,
ADD KEY `idx_commentaire_parent` (`id_commentaire_parent`),
ADD CONSTRAINT `fk_commentaire_parent` 
  FOREIGN KEY (`id_commentaire_parent`) 
  REFERENCES `commentaire` (`id_commentaire`) 
  ON DELETE CASCADE 
  ON UPDATE CASCADE;
```

## Features Implemented

### 1. **Reply Button on Comments**
   - Each comment now has a "Reply" button
   - Clicking it shows an inline reply input field
   - Replies are indented and visually distinct from main comments

### 2. **Nested Comment Display**
   - Main comments (top-level) are displayed normally
   - Replies are indented 30px to the right
   - Replies have smaller avatars (12px vs 14px)
   - Different color scheme for replies (lighter purple)

### 3. **Reply Input**
   - Inline reply input appears when clicking "Reply"
   - Has "Send" and "Cancel" buttons
   - Pressing Enter sends the reply
   - Cancel button removes the input field

### 4. **Comment Count**
   - The comment counter now includes both main comments AND replies
   - Shows accurate total count

### 5. **Delete Functionality**
   - Users can delete their own replies
   - Admins can delete any reply
   - Deleting a parent comment deletes all its replies (CASCADE)

## Code Changes

### Files Modified:

1. **`entities/Commentaire.java`**
   - Added `idCommentaireParent` field
   - Added constructors supporting parent comment ID

2. **`services/CommentaireService.java`**
   - Updated `add()` to support parent comment ID
   - Modified `getByPost()` to only return top-level comments
   - Added `getReplies()` method to fetch replies for a comment
   - Updated `mapRow()` to handle parent comment field

3. **`controllers/CommunityController.java`**
   - Refactored comment display into `buildCommentBlock()`
   - Added `buildReplyRow()` for rendering replies
   - Added `showReplyInput()` for inline reply input
   - Updated comment counting to include replies

### Files Created:

1. **`add_comment_replies.sql`** - Database migration script

## How It Works

1. **Top-level comments** have `id_commentaire_parent = NULL`
2. **Replies** have `id_commentaire_parent` set to the parent comment's ID
3. When loading comments:
   - First, load all top-level comments (parent = NULL)
   - For each top-level comment, load its replies
   - Display replies indented below the parent

## Visual Design

- **Main comments**: Purple avatar (14px), normal text
- **Replies**: Light purple avatar (12px), smaller text, indented
- **Reply button**: Subtle gray, turns blue on hover
- **Reply input**: Light gray background, smaller than main comment input

## Testing

After running the migration, test:
1. ✅ Create a new comment on a post
2. ✅ Click "Reply" on a comment
3. ✅ Type a reply and send it
4. ✅ Verify the reply appears indented below the comment
5. ✅ Verify comment count includes replies
6. ✅ Delete a reply (if you own it)
7. ✅ Delete a parent comment (should delete replies too)

## Notes

- The database CASCADE constraint ensures that deleting a parent comment automatically deletes all its replies
- The UI prevents showing multiple reply inputs on the same comment
- Reply inputs auto-focus when opened for better UX
- All existing comments will have `id_commentaire_parent = NULL` (top-level)
