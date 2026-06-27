# Technical Plan: App UI/UX Remake

**Feature**: Complete UI/UX redesign while preserving existing color palette  
**Branch**: `001-ui-ux-remake`  
**Created**: 2026-01-24  
**Status**: Planning

## Overview

This plan outlines a comprehensive UI/UX remake of the Klarity app. The redesign will standardize design patterns, improve consistency, enhance usability, and create a cohesive visual language—all while keeping the existing color palette unchanged.

### Key Constraint

**Colors must remain unchanged**: All hex values in `presentation/theme/Colors.kt` (KlarityColors) and their Material 3 ColorScheme mappings in `Theme.kt` must be preserved exactly.

## Current State Analysis

### Existing Design System Assets (KEEP)

The app already has strong design system foundations:

1. **Color Palette** (`Colors.kt`):
   - Comprehensive dark teal/green theme with 80+ semantic color tokens
   - Well-organized: backgrounds, accents, text, borders, semantic, priority, tags, etc.
   - Good contrast hierarchy (BgPrimary → BgSecondary → BgTertiary)
   - Extended colors for AI, timers, syntax highlighting, status indicators

2. **Typography** (`Typography.kt`):
   - Material 3 Typography scale (display, headline, title, body, label)
   - Custom text styles for code/mono/editor
   - Good line-height ratios (1.75x for body text)

3. **Spacing** (`Theme.kt`):
   - Spacing tokens (4dp base: extraSmall=4, small=8, medium=16, large=24, extraLarge=32, huge=48, massive=64)
   - Available via `KlarityTheme.spacing`

4. **Shapes** (`Shapes.kt`):
   - KlarityShapes tokens (ExtraSmall=4dp, Small=8dp, Medium=12dp, Large=16dp, ExtraLarge=28dp, Full=50%)
   - Material 3 Shapes scale in Theme

5. **Motion** (`Theme.kt`):
   - KlarityMotion with M3 easing curves (Standard, Emphasized, EmphasizedDecelerate/Accelerate)
   - Duration tokens (Short1-4, Medium1-4, Long1-4, ExtraLong1-4)
   - Pre-built animation specs

6. **Extended Colors & Locals** (`Theme.kt`):
   - ExtendedColors for semantic colors beyond M3 (success, warning, info, accentAI, borderSelected, textTertiary, textMuted)
   - LocalExtendedColors and LocalSpacing composition locals
   - ColorScheme extensions for backward compat

### Current UI Structure

Based on codebase exploration:

**Navigation Architecture**:
- Two-layer navigation:
  1. Top-level: Compose Navigation NavHost (`KlarityNavigation.kt` + `Screen.kt` sealed interface)
     - Routes: `Home`, `Editor(noteId)`, `Settings` (declared but not wired)
  2. Internal Home workspace navigation: `NavDestination` enum in `NavigationRail.kt` (HOME/NOTES/GRAPH/TASKS/SETTINGS)
     - Controlled by state in `HomeScreen.kt`, switches panes/presets rather than NavHost routes

**Key Screens & Components**:
- `HomeScreen.kt`: Workspace shell with internal nav + multi-pane system
- `WorkspaceLayout.kt`: Adaptive workspace container, presets, `PaneType` enum, `WorkspaceLayoutMode`, `WorkspaceTopBar`
- `TopCommandBar.kt`: Top command bar + command palette
- `NavigationRail.kt`: NavDestination enum + rail UI
- `NotesListPane.kt`: Notes list pane
- `EditorPanel.kt`: In-place editor panel (inside Home workspace)
- `EditorScreen.kt`: Separate NavHost route for full-screen editor
- `HomeDashboard.kt`: Dashboard view
- `GraphScreen.kt`: Graph visualization
- `TasksScreen.kt`: Kanban board (exists but not wired into Home panes/NavHost)
  - Related: `KanbanBoard.kt`, `TaskDetailModal.kt`, `BoardControls.kt`, `TasksHeader.kt`, `TaskTimeline.kt`
- `M3Components.kt`: Reusable M3-styled components (search bar, buttons, chips, cards, list items, progress, FABs)

**Identified Issues**:
- Inconsistent use of design tokens: many screens use **hardcoded dp/sp** instead of `KlarityTheme.spacing` and typography tokens
- Multiple "search bar" implementations across the app (not standardized)
- Home workspace panes use a placeholder `TasksPane()` instead of the full `TasksScreen`
- Settings route declared but not implemented in NavHost
- Potential duplicated UI patterns (search bars, toolbars, chips/cards, paddings)

## Design System Strategy

### Phase 1: Design System Standardization (P1)

**Goal**: Ensure all UI code uses design system tokens consistently.

#### 1.1 Typography Audit & Enforcement

**What**: Replace all hardcoded `fontSize`/`lineHeight`/`fontWeight` with Material 3 typography scale.

**How**:
1. Scan all `*.kt` files in `presentation/**` for hardcoded font properties
2. Map to M3 typography scale:
   - Hardcoded titles → `MaterialTheme.typography.titleLarge/titleMedium/titleSmall`
   - Hardcoded body → `MaterialTheme.typography.bodyLarge/bodyMedium/bodySmall`
   - Hardcoded labels → `MaterialTheme.typography.labelLarge/labelMedium/labelSmall`
   - Code text → `CustomTextStyles.Code/CodeSmall/Mono`
   - Editor text → `CustomTextStyles.EditorBody`
3. For each screen/component, replace hardcoded text styles with appropriate M3 style

**Files to audit**:
- All files in `presentation/screen/**`
- All files in `presentation/components/**`
- `presentation/navigation/**` (NavigationRail, etc.)

**Success Criteria**:
- Zero hardcoded `fontSize`/`lineHeight` in production UI code (outside of theme definition files)
- All text uses `MaterialTheme.typography.*` or `CustomTextStyles.*`

#### 1.2 Spacing Audit & Enforcement

**What**: Replace all hardcoded padding/margin `dp` values with `KlarityTheme.spacing` tokens.

**How**:
1. Scan all `*.kt` files for hardcoded `.dp` in `Modifier.padding()`, `Modifier.size()`, `Spacer(height/width)`, etc.
2. Map to spacing tokens:
   - 4dp → `KlarityTheme.spacing.extraSmall`
   - 8dp → `KlarityTheme.spacing.small`
   - 16dp → `KlarityTheme.spacing.medium`
   - 24dp → `KlarityTheme.spacing.large`
   - 32dp → `KlarityTheme.spacing.extraLarge`
   - 48dp → `KlarityTheme.spacing.huge`
   - 64dp → `KlarityTheme.spacing.massive`
3. For non-standard values (e.g., 12dp, 20dp), either:
   - Round to nearest token (preferred for consistency)
   - Add intermediate token if justified by usage frequency
4. Replace across all screens/components

**Files to audit**: Same as typography (all UI files)

**Success Criteria**:
- Zero hardcoded `.dp` values in production UI code (outside of theme definition files)
- All spacing uses `KlarityTheme.spacing.*`

#### 1.3 Shape Audit & Enforcement

**What**: Replace hardcoded corner radii with `MaterialTheme.shapes` or `KlarityShapes`.

**How**:
1. Scan for hardcoded `RoundedCornerShape(##.dp)` in UI code
2. Map to shape tokens:
   - 4dp → `MaterialTheme.shapes.extraSmall` or `KlarityShapes.ExtraSmall`
   - 8dp → `MaterialTheme.shapes.small` or `KlarityShapes.Small`
   - 12dp → `MaterialTheme.shapes.medium` or `KlarityShapes.Medium`
   - 16dp → `MaterialTheme.shapes.large` or `KlarityShapes.Large`
   - 28dp → `MaterialTheme.shapes.extraLarge` or `KlarityShapes.ExtraLarge`
   - Circular/Pill → `KlarityShapes.Full`
3. Replace across all screens/components

**Success Criteria**:
- Zero hardcoded `RoundedCornerShape(##.dp)` in production UI code
- All shapes use theme tokens

#### 1.4 Color Audit & Enforcement

**What**: Ensure all color references use theme tokens (no hardcoded `Color(0x...)` in UI code).

**How**:
1. Scan for hardcoded `Color(0x...)` literals in UI code (excluding `Colors.kt` itself)
2. Map to existing theme colors:
   - Background colors → `MaterialTheme.colorScheme.background/surface/surfaceVariant` or `KlarityColors.*`
   - Text colors → `MaterialTheme.colorScheme.onBackground/onSurface/onSurfaceVariant` or `KlarityColors.Text*`
   - Accent colors → `MaterialTheme.colorScheme.primary/secondary/tertiary` or `KlarityColors.Accent*`
   - Borders → `MaterialTheme.colorScheme.outline/outlineVariant` or `KlarityColors.Border*`
   - Semantic → `KlarityTheme.extendedColors.success/warning/info` or `MaterialTheme.colorScheme.error`
3. If a color literal is found that doesn't map to an existing token, check if it's a variation (e.g., `.copy(alpha=...)`) - preserve the pattern but use token as base
4. Flag any truly "orphan" colors for review (may indicate missing design decision)

**Files to audit**: Same as typography (all UI files)

**Success Criteria**:
- Zero hardcoded color literals in production UI code
- All colors reference theme tokens
- Any alpha variations use token as base (e.g., `MaterialTheme.colorScheme.primary.copy(alpha=0.2f)`)

#### 1.5 Motion Audit & Enforcement

**What**: Replace hardcoded animation durations with `KlarityMotion` tokens.

**How**:
1. Scan for `animate*AsState` with hardcoded `animationSpec = tween(durationMillis = ###)`
2. Map to motion tokens:
   - 50-200ms → `KlarityMotion.quickExit()` or `KlarityMotion.standardExit()`
   - 200-400ms → `KlarityMotion.standardEnter()`
   - 350-600ms → `KlarityMotion.emphasizedEnter()` or `KlarityMotion.emphasizedExit()`
3. Use appropriate easing curve based on context (enter/exit/standard/emphasized)

**Success Criteria**:
- All animations use `KlarityMotion` specs
- Consistent easing across similar interactions

---

### Phase 2: Component Standardization (P1)

**Goal**: Create a single source of truth for common UI patterns.

#### 2.1 Unified Search Bar Component

**Problem**: Multiple search bar implementations exist (`KlaritySearchBar` in `M3Components.kt`, plus ad-hoc implementations in various screens).

**Solution**:
1. Audit all search bar usages across the app
2. Consolidate into a single `KlaritySearchBar` component in `M3Components.kt` (or new `SearchComponents.kt`)
3. Support common variants via parameters:
   - Basic search (just input + icon)
   - Search with filters (input + filter chips)
   - Search with recent/suggestions
4. Replace all ad-hoc search implementations with the unified component

**Files**:
- Define: `presentation/components/SearchComponents.kt` (or extend `M3Components.kt`)
- Replace in: `NotesListPane.kt`, `TopCommandBar.kt` (command palette search), any other screens with search

#### 2.2 Unified Card Components

**Problem**: Cards are used throughout (notes list, tasks, dashboard) with inconsistent styling.

**Solution**:
1. Define card variants in `M3Components.kt`:
   - `KlarityCard`: Base card with standard elevation/border/padding
   - `KlarityClickableCard`: Interactive card with hover/pressed states
   - `KlaritySelectionCard`: Card with selected state (used in lists)
2. Standardize:
   - Background: `MaterialTheme.colorScheme.surface` or `surfaceVariant`
   - Border: `MaterialTheme.colorScheme.outline` (1.dp) or `KlarityColors.BorderPrimary`
   - Corner radius: `MaterialTheme.shapes.medium` (12.dp)
   - Padding: `KlarityTheme.spacing.medium` (16.dp)
   - Hover effect: `MaterialTheme.colorScheme.surfaceBright` + elevation
   - Selected state: `KlarityColors.BgSelected` + `KlarityColors.BorderSelected` border
3. Replace all card implementations with unified components

**Files**:
- Define: `presentation/components/CardComponents.kt` (or extend `M3Components.kt`)
- Replace in: `NotesListPane.kt`, `TasksScreen.kt` (KanbanBoard task cards), `HomeDashboard.kt`

#### 2.3 Unified Button System

**Problem**: Buttons exist in `M3Components.kt` but may not be used consistently.

**Solution**:
1. Audit all button usages (primary, secondary, text, icon buttons)
2. Ensure all buttons use components from `M3Components.kt`:
   - `PrimaryButton`: Filled button with `MaterialTheme.colorScheme.primary` background
   - `SecondaryButton`: Outlined button
   - `TextButton`: Text-only button
   - `IconButtonKlarity`: Icon button with standard size/padding
3. Standardize interaction states:
   - Hover: subtle background change + cursor pointer
   - Pressed: slightly darker background
   - Disabled: reduced opacity + `MaterialTheme.colorScheme.onSurface.copy(alpha=0.38f)`
4. Replace all ad-hoc button implementations

**Files**:
- Audit/extend: `M3Components.kt`
- Replace in: All screens

#### 2.4 Unified Input Components

**Problem**: Text inputs, dropdowns, toggles may have inconsistent styling.

**Solution**:
1. Define input variants:
   - `KlarityTextField`: Standard text input with label, placeholder, error states
   - `KlarityOutlinedTextField`: Outlined variant
   - `KlarityDropdown`: Dropdown/select component
   - `KlaritySwitch`: Toggle switch with consistent styling
2. Standardize:
   - Border: `MaterialTheme.colorScheme.outline` (default), `MaterialTheme.colorScheme.primary` (focused)
   - Text: `MaterialTheme.typography.bodyLarge`
   - Label: `MaterialTheme.typography.labelMedium`
   - Error: `MaterialTheme.colorScheme.error` with helper text
3. Ensure all inputs support:
   - Focus state
   - Error state
   - Disabled state
   - Clear button (where appropriate)

**Files**:
- Define: `presentation/components/InputComponents.kt` (or extend `M3Components.kt`)
- Replace in: All forms/editors

#### 2.5 Unified List Item Component

**Problem**: List items (notes, tasks, etc.) have varying layouts.

**Solution**:
1. Define `KlarityListItem` component with slots:
   - Leading icon/avatar
   - Title (primary text)
   - Subtitle (secondary text)
   - Trailing icon/action
   - Metadata (timestamp, tags, etc.)
2. Support variants:
   - Basic list item
   - Two-line list item
   - Three-line list item
   - Clickable list item (with hover/pressed states)
   - Selection list item (with checkbox/radio)
3. Standardize:
   - Padding: `KlarityTheme.spacing.medium` horizontal, `KlarityTheme.spacing.small` vertical
   - Title: `MaterialTheme.typography.bodyLarge`
   - Subtitle: `MaterialTheme.typography.bodyMedium` + `MaterialTheme.colorScheme.onSurfaceVariant`
   - Metadata: `MaterialTheme.typography.labelSmall` + `KlarityTheme.extendedColors.textTertiary`

**Files**:
- Define: `presentation/components/ListComponents.kt` (or extend `M3Components.kt`)
- Replace in: `NotesListPane.kt`, any other list views

---

### Phase 3: Navigation UX Improvements (P2)

**Goal**: Make navigation predictable and consistent across the app.

#### 3.1 Unify Navigation Architecture

**Problem**: Two-layer navigation (NavHost + internal Home workspace nav) can be confusing.

**Analysis**:
- Current: NavHost has `Home`, `Editor(noteId)`, `Settings` routes
- Current: Home internally switches between HOME/NOTES/GRAPH/TASKS/SETTINGS via `NavDestination` state
- Issue: "Settings" exists in both layers (NavHost route declared but not wired; also in `NavDestination`)

**Recommendation**:
1. **Keep two-layer navigation** but clarify responsibilities:
   - NavHost: Routes for *full-screen* modes (`Home`, `Editor(noteId)`)
   - Internal Home nav: Workspace panes within `Home` (HOME dashboard, NOTES list, GRAPH, TASKS board, SETTINGS panel)
2. **Remove NavHost `Settings` route** (not needed; settings can live as a pane inside Home workspace)
3. **Wire up TASKS pane properly**: Replace placeholder `TasksPane()` in `WorkspaceLayout.kt` with full `TasksScreen` content

**Implementation**:
1. Update `Screen.kt`: Remove `Settings` from sealed interface
2. Update `KlarityNavigation.kt`: Remove Settings route from NavHost
3. Update `WorkspaceLayout.kt`:
   - Replace `PaneType.TASKS` case with proper `TasksScreen()` composable
   - Ensure `PaneType.SETTINGS` case has a proper settings UI (create `SettingsPane.kt` if needed)
4. Update `NavigationRail.kt`:
   - Ensure `NavDestination.SETTINGS` is wired to show settings pane
   - Visual feedback: active destination highlights with `KlarityColors.BorderSelected` + `MaterialTheme.colorScheme.primary`

**Files**:
- `presentation/navigation/Screen.kt`
- `presentation/navigation/KlarityNavigation.kt`
- `presentation/screen/home/WorkspaceLayout.kt`
- `presentation/navigation/NavigationRail.kt`
- Create: `presentation/screen/settings/SettingsPane.kt` (if doesn't exist)

#### 3.2 Navigation State Indicators

**Problem**: User may not always know where they are (especially within multi-pane Home workspace).

**Solution**:
1. **Navigation rail active state**:
   - Active item: background `KlarityColors.BgSelected` + border `KlarityColors.BorderSelected` + icon color `MaterialTheme.colorScheme.primary`
   - Inactive item: transparent background + icon color `MaterialTheme.colorScheme.onSurfaceVariant`
   - Hover: subtle background `MaterialTheme.colorScheme.surfaceContainerHigh`
2. **Top bar context indicator** (if applicable):
   - Show current pane name in `TopCommandBar` (e.g., "Notes" / "Tasks" / "Graph")
   - Breadcrumb-style navigation for nested views
3. **Back behavior**:
   - Within workspace: navigate back to HOME dashboard pane
   - From Editor route: navigate back to Home route (notes pane if coming from notes list)

**Files**:
- `presentation/navigation/NavigationRail.kt`
- `presentation/screen/home/TopCommandBar.kt`
- `presentation/navigation/KlarityNavigation.kt` (back stack management)

#### 3.3 Navigation Transitions

**Problem**: Abrupt screen changes feel jarring.

**Solution**:
1. Add subtle crossfade/slide transitions between workspace panes using `KlarityMotion`:
   - Pane enter: `KlarityMotion.emphasizedEnter()` with `fadeIn + slideInHorizontally`
   - Pane exit: `KlarityMotion.emphasizedExit()` with `fadeOut + slideOutHorizontally`
2. Use `AnimatedContent` or `Crossfade` composable for pane switching in `WorkspaceLayout.kt`

**Files**:
- `presentation/screen/home/WorkspaceLayout.kt`

---

### Phase 4: Screen-by-Screen Redesign (P1-P3)

**Goal**: Apply design system and component standardization to every screen.

#### 4.1 Home Workspace (P1)

**Files**: `HomeScreen.kt`, `WorkspaceLayout.kt`, `TopCommandBar.kt`, `NavigationRail.kt`

**Changes**:
1. **HomeScreen.kt**:
   - Replace hardcoded padding/spacing with `KlarityTheme.spacing` tokens
   - Ensure layout uses Material 3 Surface containers (`surfaceContainerLow`, `surfaceContainer`, etc.) for proper elevation hierarchy
2. **WorkspaceLayout.kt**:
   - Standardize pane background colors (use `MaterialTheme.colorScheme.surface` or `surfaceVariant`)
   - Apply consistent padding to all panes using `KlarityTheme.spacing.medium`
   - Add pane transition animations
   - Wire up TASKS pane to full `TasksScreen`
   - Wire up SETTINGS pane to `SettingsPane`
3. **TopCommandBar.kt**:
   - Replace hardcoded search bar with unified `KlaritySearchBar` component
   - Standardize command palette styling (modal, overlay, list items)
   - Use typography tokens for command text
4. **NavigationRail.kt**:
   - Standardize active/inactive/hover states (colors, shapes, spacing)
   - Use icon button component from `M3Components.kt`
   - Ensure rail uses `MaterialTheme.colorScheme.surfaceContainerLow` for background

#### 4.2 Notes List Pane (P1)

**Files**: `NotesListPane.kt`

**Changes**:
1. Replace search bar with unified `KlaritySearchBar`
2. Replace note cards with unified `KlaritySelectionCard` (or `KlarityClickableCard`)
3. Standardize list item layout:
   - Title: `MaterialTheme.typography.titleMedium`
   - Preview/body: `MaterialTheme.typography.bodyMedium` + `MaterialTheme.colorScheme.onSurfaceVariant`
   - Timestamp: `MaterialTheme.typography.labelSmall` + `KlarityTheme.extendedColors.textTertiary`
   - Tags: use unified chip component
4. Standardize spacing:
   - List padding: `KlarityTheme.spacing.medium`
   - Item spacing: `KlarityTheme.spacing.small`
   - Card padding: `KlarityTheme.spacing.medium`
5. Add empty state:
   - Icon + message ("No notes yet")
   - Primary action button ("Create your first note")
6. Add loading state (skeleton/shimmer or progress indicator)
7. Hover/selection states:
   - Hover: subtle background lift + `MaterialTheme.colorScheme.surfaceBright`
   - Selected: `KlarityColors.BgSelected` + `KlarityColors.BorderSelected` border

#### 4.3 Editor Panel (P1)

**Files**: `EditorPanel.kt`, `EditorScreen.kt`

**Changes**:
1. Standardize editor layout:
   - Title input: `MaterialTheme.typography.headlineMedium` (or `titleLarge`)
   - Body input: `CustomTextStyles.EditorBody` (16sp, 28.8sp line height)
   - Toolbar: consistent height/padding using `KlarityTheme.spacing`
2. Toolbar buttons:
   - Use unified icon buttons from `M3Components.kt`
   - Hover/active states consistent with system
3. Markdown rendering:
   - Headings: `MaterialTheme.typography.headlineLarge/headlineMedium/headlineSmall`
   - Body: `MaterialTheme.typography.bodyLarge`
   - Code blocks: `CustomTextStyles.Code` + `KlarityColors.BgCode` background
   - Links: `KlarityColors.TextLink` with underline on hover
4. Empty state: "Start writing..." placeholder in `MaterialTheme.colorScheme.onSurfaceVariant`
5. Ensure editor uses `MaterialTheme.colorScheme.surface` or `KlarityColors.BgEditor` for background

#### 4.4 Home Dashboard (P2)

**Files**: `HomeDashboard.kt`

**Changes**:
1. Standardize dashboard cards:
   - Use unified `KlarityCard` component
   - Card title: `MaterialTheme.typography.titleMedium`
   - Card content: `MaterialTheme.typography.bodyMedium`
   - Card spacing: `KlarityTheme.spacing.medium` padding
2. Widgets:
   - Recent notes: list with unified list item component
   - Quick stats: card with icon + number + label
   - Recent activity: timeline with consistent typography/spacing
3. Empty state: "Welcome to Klarity" + onboarding hints
4. Layout: responsive grid (1 column on compact, 2-3 columns on expanded)

#### 4.5 Graph Screen (P2)

**Files**: `GraphScreen.kt`

**Changes**:
1. Standardize controls:
   - Zoom/pan controls: unified icon buttons
   - Search/filter: unified `KlaritySearchBar`
   - Legend: card with consistent typography
2. Node styling:
   - Node background: `MaterialTheme.colorScheme.surfaceVariant`
   - Node border: `MaterialTheme.colorScheme.outline`
   - Node text: `MaterialTheme.typography.labelMedium`
   - Selected node: `KlarityColors.BorderSelected` border + `MaterialTheme.colorScheme.primary` text
3. Edge styling:
   - Edge color: `MaterialTheme.colorScheme.outlineVariant`
   - Highlighted edge: `MaterialTheme.colorScheme.primary`
4. Empty state: "No connections yet" + hint to link notes
5. Loading state: spinner + "Building graph..."

#### 4.6 Tasks Screen (P2)

**Files**: `TasksScreen.kt`, `KanbanBoard.kt`, `TaskDetailModal.kt`, `BoardControls.kt`, `TasksHeader.kt`, `TaskTimeline.kt`

**Changes**:
1. **KanbanBoard.kt**:
   - Column background: `MaterialTheme.colorScheme.surfaceContainerLow`
   - Column header: `MaterialTheme.typography.titleMedium` + count badge
   - Column border: `MaterialTheme.colorScheme.outline`
   - Column spacing: `KlarityTheme.spacing.small`
   - Task cards: unified `KlarityClickableCard`
   - Drag-and-drop feedback: animated elevation/shadow/border
2. **Task cards**:
   - Title: `MaterialTheme.typography.bodyLarge`
   - Description: `MaterialTheme.typography.bodyMedium` + `MaterialTheme.colorScheme.onSurfaceVariant`
   - Priority dot: `KlarityColors.PriorityHigh/Medium/Low` with 8dp size
   - Due date: `MaterialTheme.typography.labelSmall` + semantic color (overdue=error, due soon=warning)
   - Tags: unified chip component
3. **TaskDetailModal.kt**:
   - Modal background: `MaterialTheme.colorScheme.surfaceContainerHighest` (elevated)
   - Modal shape: `KlarityShapes.ExtraLargeTop` (28dp rounded top corners)
   - Title input: `MaterialTheme.typography.titleLarge`
   - Fields: unified input components
   - Buttons: unified button components
4. **BoardControls.kt**:
   - Filter/sort/group buttons: unified chip/button components
   - Consistent spacing: `KlarityTheme.spacing.small` between controls
5. **TasksHeader.kt**:
   - Title: `MaterialTheme.typography.headlineMedium`
   - Actions: unified icon buttons
6. Empty state: "No tasks yet" + "Add your first task" button
7. Loading state: skeleton columns + shimmer effect

#### 4.7 Settings Pane (P3)

**Files**: Create `presentation/screen/settings/SettingsPane.kt`

**Changes**:
1. Settings list:
   - Section headers: `MaterialTheme.typography.labelLarge` + `KlarityTheme.extendedColors.textTertiary`
   - Setting items: unified list item component
   - Toggles/switches: `KlaritySwitch` from input components
   - Dropdowns: `KlarityDropdown` from input components
2. Settings sections:
   - Appearance (theme toggle—but note: we're dark-only currently, so this may be disabled/hidden)
   - Notifications
   - Data & sync
   - About (app version, links)
3. Layout: single column list with dividers between sections
4. Spacing: `KlarityTheme.spacing.medium` for section padding

---

### Phase 5: Empty/Loading/Error States (P2)

**Goal**: Every screen has meaningful feedback for empty, loading, and error states.

#### 5.1 Empty States

**Standard pattern**:
- Icon (from icon library, 48dp size)
- Heading: `MaterialTheme.typography.titleMedium`
- Description: `MaterialTheme.typography.bodyMedium` + `MaterialTheme.colorScheme.onSurfaceVariant`
- Primary action button (if applicable): unified `PrimaryButton`

**Implement for**:
- Notes list: "No notes yet" + "Create your first note"
- Tasks board: "No tasks yet" + "Add your first task"
- Graph: "No connections yet" + "Start linking notes"
- Search results: "No results found" + "Try a different search"
- Dashboard: "Welcome to Klarity" + onboarding hints

**Files**: Each screen file (as listed in Phase 4)

#### 5.2 Loading States

**Standard pattern**:
- Spinner: `CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)`
- Message: `MaterialTheme.typography.bodyMedium` + `MaterialTheme.colorScheme.onSurfaceVariant`
- Alternative: Skeleton UI (shimmer effect) for lists/cards

**Implement for**:
- Notes list: skeleton note cards
- Tasks board: skeleton columns + task cards
- Graph: spinner + "Building graph..."
- Dashboard: skeleton widgets
- Editor: spinner (if loading note from remote)

**Files**: Each screen file (as listed in Phase 4)

#### 5.3 Error States

**Standard pattern**:
- Error icon (red, 48dp)
- Heading: `MaterialTheme.typography.titleMedium` + `MaterialTheme.colorScheme.error`
- Description: `MaterialTheme.typography.bodyMedium`
- Retry button: unified `PrimaryButton`

**Implement for**:
- Notes list: "Failed to load notes" + "Retry"
- Tasks board: "Failed to load tasks" + "Retry"
- Graph: "Failed to build graph" + "Retry"
- Editor: "Failed to save note" (inline error, not full-screen)

**Files**: Each screen file (as listed in Phase 4)

---

### Phase 6: Iconography & Visual Polish (P3)

**Goal**: Consistent icon style and sizing across the app.

#### 6.1 Icon Audit

**What**: Ensure all icons come from a single icon library (e.g., Material Icons, Heroicons, Lucide).

**How**:
1. Identify current icon usage (scan for `Icon(imageVector = ...)`)
2. Check if icons are from a consistent library
3. If mixed sources, consolidate to one library (prefer Material Icons for Material 3 app)
4. Standardize icon size:
   - Small icons (list items, buttons): 20dp
   - Medium icons (toolbar, nav rail): 24dp
   - Large icons (empty states, headers): 48dp

**Files**: All UI files

#### 6.2 Hover/Focus States

**What**: Ensure all interactive elements have clear hover/focus feedback.

**How**:
1. Audit all clickable elements (buttons, cards, list items, icons)
2. Add hover indication:
   - Background change (subtle lift or color shift)
   - Cursor pointer (if web/desktop)
   - Animated transition using `KlarityMotion.quickExit()` or `KlarityMotion.standardEnter()`
3. Add focus ring for keyboard navigation:
   - Border: `MaterialTheme.colorScheme.primary` (2dp)
   - Shape: match element shape

**Files**: All interactive components

#### 6.3 Micro-interactions

**What**: Add subtle animations to improve perceived responsiveness.

**Examples**:
- Button press: scale down slightly (0.95x) + darken background
- Card tap: brief elevation lift
- Switch toggle: smooth thumb slide + track color change
- List item selection: checkmark fade-in
- Modal open/close: slide + fade using `KlarityMotion.emphasizedEnter/Exit()`

**How**:
1. Identify key interactions
2. Add `animateContentSize()`, `animateColorAsState()`, `Animatable`, etc. with `KlarityMotion` specs
3. Ensure animations respect `prefers-reduced-motion` (check system settings and disable non-essential animations)

**Files**: Component files (`M3Components.kt`, etc.)

---

## Implementation Order

### Sprint 1: Design System Foundation (P1)

**Week 1-2**:
1. Phase 1.1: Typography audit & enforcement
2. Phase 1.2: Spacing audit & enforcement
3. Phase 1.3: Shape audit & enforcement
4. Phase 1.4: Color audit & enforcement
5. Phase 1.5: Motion audit & enforcement

**Deliverable**: All UI code uses design system tokens (no hardcoded values).

### Sprint 2: Component Library (P1)

**Week 3-4**:
1. Phase 2.1: Unified search bar
2. Phase 2.2: Unified card components
3. Phase 2.3: Unified button system
4. Phase 2.4: Unified input components
5. Phase 2.5: Unified list item component

**Deliverable**: Complete component library in `presentation/components/`.

### Sprint 3: Navigation & Core Screens (P1)

**Week 5-6**:
1. Phase 3.1: Unify navigation architecture
2. Phase 3.2: Navigation state indicators
3. Phase 3.3: Navigation transitions
4. Phase 4.1: Home workspace redesign
5. Phase 4.2: Notes list pane redesign
6. Phase 4.3: Editor panel redesign

**Deliverable**: Core note-taking flow (P1 user story) fully redesigned.

### Sprint 4: Secondary Screens (P2)

**Week 7-8**:
1. Phase 4.4: Home dashboard redesign
2. Phase 4.5: Graph screen redesign
3. Phase 4.6: Tasks screen redesign
4. Phase 5.1: Empty states for all screens
5. Phase 5.2: Loading states for all screens
6. Phase 5.3: Error states for all screens

**Deliverable**: All screens redesigned with proper states.

### Sprint 5: Settings & Polish (P3)

**Week 9-10**:
1. Phase 4.7: Settings pane implementation
2. Phase 6.1: Icon audit
3. Phase 6.2: Hover/focus states
4. Phase 6.3: Micro-interactions
5. Final QA pass: test all screens, fix inconsistencies

**Deliverable**: Fully polished, consistent UI/UX across the entire app.

---

## Testing Strategy

### Unit Tests

**What**: Test design system tokens and component behavior.

**Where**: `composeApp/src/commonTest/kotlin/com/example/klarity/presentation/`

**Tests**:
1. Theme tokens: verify `KlarityTheme.spacing`, `KlarityTheme.extendedColors` provide expected values
2. Component state: verify button/card/input components handle state changes correctly (disabled, selected, focused, etc.)
3. Color contrast: verify text colors meet WCAG AA contrast ratio (4.5:1 for body text, 3:1 for large text)

### UI Tests

**What**: Test critical user flows (P1 journeys) end-to-end.

**Where**: `composeApp/src/androidTest/kotlin/com/example/klarity/` (Android) + shared tests if possible

**Tests**:
1. User Story 1 (Confident Note Work):
   - Open app → see clear primary action → find a note → open editor → make edit → verify UI consistency
2. User Story 2 (Predictable Navigation):
   - Switch between workspace panes → verify active indicator → use back navigation → verify correct behavior
3. User Story 3 (Cohesive System Feel):
   - Visit multiple screens → verify consistent components/typography/spacing

### Accessibility Tests

**What**: Verify redesign doesn't introduce accessibility regressions.

**Tests**:
1. Screen reader: verify all interactive elements have content descriptions
2. Keyboard navigation: verify all actions are reachable via keyboard (focus order, focus visible)
3. Touch targets: verify all interactive elements meet minimum touch target size (48dp)
4. Text scaling: verify UI remains usable at 200% text size
5. Reduced motion: verify animations can be disabled

---

## Success Criteria Mapping

**From spec.md**:

- **SC-001**: In a usability test, at least 90% of participants can open an existing note and make an edit on the first attempt without assistance.
  - **Plan**: Phase 4.2 (Notes List) + Phase 4.3 (Editor) ensure clear affordances and consistent interaction patterns.

- **SC-002**: In a usability test, at least 90% of participants can navigate between three major areas and correctly identify their current location each time.
  - **Plan**: Phase 3.2 (Navigation State Indicators) ensures active state is clearly visible in nav rail + top bar.

- **SC-003**: Across defined key screens, all primary actions are reachable within 2 interactions from the initial screen state (tap/click/keyboard action).
  - **Plan**: Phase 4 (Screen-by-Screen Redesign) places primary actions prominently (e.g., "Create note" FAB, "Add task" button).

- **SC-004**: User-reported satisfaction with visual clarity and consistency averages at least 4.2/5 in a post-task survey (n >= 20).
  - **Plan**: Phase 1 (Design System Standardization) + Phase 2 (Component Standardization) ensure visual consistency.

- **SC-005**: No critical accessibility regressions are introduced: all interactive controls have discernible labels, and text remains readable at increased text size.
  - **Plan**: Accessibility testing strategy + Phase 6.2 (Hover/Focus States) ensure accessibility.

---

## Risk Mitigation

### Risk 1: Breaking Existing Functionality

**Mitigation**:
- Incremental rollout: implement phase-by-phase, test after each phase
- Comprehensive UI tests for critical flows before and after each change
- Feature flags (if available) to enable/disable redesigned screens during development

### Risk 2: Inconsistent Application of Design System

**Mitigation**:
- Design system enforcement: create linter rules or pre-commit hooks to flag hardcoded values
- Code review checklist: verify all new/changed UI code uses design tokens
- Regular audits: scan codebase for hardcoded values after each sprint

### Risk 3: Scope Creep

**Mitigation**:
- Strict scope boundaries defined in spec (no new capabilities, only presentation changes)
- Prioritization: P1 (core flows) → P2 (secondary screens) → P3 (polish)
- Time-box each phase: if a phase overruns, defer lower-priority items to later sprint

### Risk 4: Color Contrast Issues

**Mitigation**:
- Verify existing color palette meets WCAG AA contrast ratios (already defined in `Colors.kt`)
- If contrast is insufficient, use layout/typography/weight to improve readability (per spec assumption)
- Test with automated contrast checking tools (e.g., Accessibility Scanner on Android)

---

## Dependencies

### Internal Dependencies

- Design system tokens: `presentation/theme/Colors.kt`, `Theme.kt`, `Typography.kt`, `Shapes.kt` (no changes needed)
- Component library: `presentation/components/M3Components.kt` (extend with new components)
- Navigation: `presentation/navigation/` (refactor NavHost + internal nav)

### External Dependencies

- Compose Material 3: already in use (verify latest stable version)
- Icon library: verify current icon library or add if missing (e.g., Material Icons Compose)
- Animation library: Compose animations (already available)

### Platform Dependencies

- Android: Compose for Android (already set up)
- iOS (if applicable): Compose Multiplatform for iOS (already set up in KMP structure)
- Desktop (if applicable): Compose for Desktop (already set up in KMP structure)

---

## Open Questions

1. **Icon library**: Which icon library is currently used? If none, which should we standardize on? (Recommendation: Material Icons for Material 3 consistency)
2. **Settings content**: What settings should be available in the Settings pane? (e.g., theme toggle, notifications, data sync, about)
3. **Reduced motion**: Does the app currently respect system reduced-motion preferences? If not, should we add support?
4. **Onboarding**: Should the Home Dashboard show onboarding hints for new users? If so, what content?
5. **Feature flags**: Are feature flags available to enable/disable redesigned screens during development?

---

## Next Steps

1. **Review this plan** with stakeholders and get approval
2. **Set up project tracking**: Create tasks/tickets for each phase in project management tool
3. **Assign sprint ownership**: Identify developers for each sprint
4. **Start Sprint 1**: Begin Phase 1.1 (Typography audit)
5. **Schedule check-ins**: Weekly progress reviews to ensure alignment

---

## Appendix: Design System Token Reference

### Typography Scale (MaterialTheme.typography)

- `displayLarge`: 57sp, Bold (rarely used)
- `displayMedium`: 45sp, Bold (rarely used)
- `displaySmall`: 36sp, Bold (hero headers)
- `headlineLarge`: 32sp, Bold (page titles)
- `headlineMedium`: 28sp, SemiBold (section headers)
- `headlineSmall`: 24sp, SemiBold (card headers)
- `titleLarge`: 22sp, SemiBold (dialog titles, prominent titles)
- `titleMedium`: 16sp, Medium (list titles, card titles)
- `titleSmall`: 14sp, Medium (small titles)
- `bodyLarge`: 16sp, Regular (primary body text)
- `bodyMedium`: 14sp, Regular (secondary body text)
- `bodySmall`: 12sp, Regular (captions, timestamps)
- `labelLarge`: 14sp, Medium (button labels, tabs)
- `labelMedium`: 12sp, Medium (small button labels, chips)
- `labelSmall`: 11sp, Medium (tiny labels)

### Spacing Scale (KlarityTheme.spacing)

- `none`: 0dp
- `extraSmall`: 4dp
- `small`: 8dp
- `medium`: 16dp
- `large`: 24dp
- `extraLarge`: 32dp
- `huge`: 48dp
- `massive`: 64dp

### Shape Scale (MaterialTheme.shapes / KlarityShapes)

- `extraSmall`: 4dp (badges, dots)
- `small`: 8dp (buttons, small cards)
- `medium`: 12dp (cards, inputs)
- `large`: 16dp (panels, large cards)
- `extraLarge`: 28dp (modals, bottom sheets)
- `Full`: 50% (circular, pill buttons)

### Motion Duration Tokens (KlarityMotion.Duration)

- `Short1-4`: 50-200ms (quick interactions)
- `Medium1-4`: 250-400ms (standard interactions)
- `Long1-4`: 450-600ms (emphasized interactions)
- `ExtraLong1-4`: 700-1000ms (hero/focal animations)

### Color Token Summary (KlarityColors)

**Backgrounds**: `BgPrimary`, `BgSecondary`, `BgTertiary`, `BgEditor`, `BgElevated`, `BgCard`, `BgNoteCard`, `BgSelected`, `BgCode`, `BgPill`

**Accents**: `AccentPrimary`, `AccentSecondary`, `AccentTertiary`, `AccentAI`, `LuminousTeal`, `ElectricMint`

**Text**: `TextPrimary`, `TextSecondary`, `TextTertiary`, `TextMuted`, `TextDisabled`, `TextLink`

**Borders**: `BorderPrimary`, `BorderSecondary`, `BorderFocus`, `BorderSelected`, `BorderDashed`

**Semantic**: `Success`, `Warning`, `Error`, `Info`, `Danger`

**Priority**: `PriorityHigh`, `PriorityMedium`, `PriorityLow`, `PriorityNone`

**Tags**: `TagResearch`, `TagUIDesign`, `TagBackend`, `TagMarketing`, `TagAnalysis`, `TagHighEffort`

**Status**: `StatusInProgress`, `StatusCompleted`, `StatusOnHold`, `StatusArchived`, `StatusSyncing`

**Buttons**: `ButtonPrimary`, `ButtonPrimaryText`, `ButtonSecondary`, `ButtonSecondaryText`, `ButtonGhost`, `ButtonGhostText`

**Sidebar**: `SidebarBg`, `SidebarItemHover`, `SidebarItemActive`, `SidebarSectionHeader`

**Editor**: `EditorBg`, `EditorCursor`, `EditorSelection`, `EditorLineHighlight`

**Syntax**: `SyntaxKeyword`, `SyntaxString`, `SyntaxComment`, `SyntaxFunction`, `SyntaxNumber`, `SyntaxOperator`, `SyntaxVariable`, `SyntaxType`

**Overlays**: `ModalOverlay`, `OverlayLight`

**Switch**: `SwitchTrackOn`, `SwitchTrackOff`, `SwitchThumb`

**Scrollbar**: `ScrollbarTrack`, `ScrollbarThumb`, `ScrollbarThumbHover`

**Timer**: `TimerBg`, `TimerText`, `TimerActive`

**Gradients**: `AccentGradient`, `AIGradient`, `CardGradient`
