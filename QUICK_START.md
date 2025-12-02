# 🚀 Sentio Quick Start Guide

## Run the App

```bash
./gradlew :composeApp:run
```

On Windows:
```cmd
gradlew.bat :composeApp:run
```

## First Time Setup

The app will automatically:
1. Create database at `~/.sentio/sentio.db`
2. Initialize empty note collection
3. Open the main window

## Basic Usage

### Create a Note
1. Click the **+** (FAB) button in the top-right
2. Enter your note title
3. Type your content
4. Note auto-saves as you type

### Search Notes
1. Type in the search bar at the top
2. Results filter in real-time
3. Click any note to open it

### Edit a Note
1. Click on any note card
2. Edit in the split-view editor
3. Changes save automatically
4. Click back arrow to return to list

### Pin/Favorite Notes
1. Open a note in the editor
2. Click the ⭐ icon to pin
3. Click the ❤️ icon to favorite

## Keyboard Shortcuts (Coming Soon)
- `Cmd/Ctrl + N` - New note
- `Cmd/Ctrl + K` - Focus search
- `Cmd/Ctrl + S` - Save (auto-saves already)
- `Esc` - Go back

## Data Location

Your notes are stored locally at:
- **macOS/Linux**: `~/.sentio/sentio.db`
- **Windows**: `C:\Users\<username>\.sentio\sentio.db`

## Troubleshooting

### App won't start
```bash
./gradlew clean
./gradlew :composeApp:run
```

### Database errors
Delete the database file and restart:
```bash
rm ~/.sentio/sentio.db
./gradlew :composeApp:run
```

### Build errors
```bash
./gradlew clean build
```

## What's Working

✅ Create, read, update, delete notes  
✅ Full-text search  
✅ Pin and favorite notes  
✅ Persistent storage  
✅ Auto-save  
✅ Reactive UI updates  

## What's Coming (Phase 2)

🚧 Markdown rendering  
🚧 Syntax highlighting  
🚧 Folder management  
🚧 Tag management  
🚧 Image embedding  
🚧 Export to PDF/HTML  

## Need Help?

- Check [GETTING_STARTED.md](GETTING_STARTED.md) for detailed development guide
- See [ARCHITECTURE.md](ARCHITECTURE.md) for technical details
- Read [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) for what's been built

---

**Enjoy using Sentio! 🌿**
