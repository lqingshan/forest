# Forest Workspace Design

## Theme System

This workspace has **two official global themes**:

- `paper` (default)
- `figma`

Every app must choose one of these presets.

These themes only control **design tokens**:

- color
- typography
- radius
- shadow
- focus and motion tone

These themes must **not** change:

- page layout
- navigation placement
- card structure
- form structure
- table structure
- route flow

The product should always feel like an operational workspace for managing:

- users
- points
- leads

Regardless of theme, the interface should feel like a **working system for operations**, not a marketing site and not a consumer app.

## Shared Product Interpretation

All themes should preserve the same product reading:

- users are dossiers
- points are ledger entries
- leads are trade records

The UI should feel precise, readable at high information density, and calm under daily operational use.

## Theme 01: Paper

### Direction

Use **Notion as the emotional reference**, not as a product clone.

This theme should feel like a calm operating notebook for trade work:

- quiet
- editorial
- paper-like
- precise
- warm

The interface should feel closer to a **curated workspace dossier** than a generic SaaS dashboard.

### Visual Principles

- Use a warm, low-contrast light theme.
- Favor off-white paper backgrounds over pure white.
- Prefer restrained borders over heavy shadows.
- Let typography and spacing carry hierarchy before color does.
- Use black, charcoal, warm gray, and muted stone as the base palette.
- Accent colors must be sparse and purposeful.

### Color Language

- Background: warm notebook paper
- Surface: slightly elevated paper cards
- Text: charcoal, not pure black
- Secondary text: warm gray
- Border: subtle charcoal line
- Accent: muted amber-brown for highlights and active navigation
- Success/Danger: softened, editorial, never neon

### Typography

- Headings should feel literary and composed.
- Body text should be neutral and highly readable.
- Avoid loud display typography.
- Use serif for major page titles and carefully selected headings.
- Use refined sans serif for body copy, controls, and dense data.

### Component Feel

Navigation:
- Quiet by default
- Clear active state
- No bright dashboard gradients

Tables:
- Minimal chrome
- Strong header clarity
- Rows should feel like structured notes, not spreadsheet boxes

Forms:
- Inputs should look soft and paper-like
- Focus states should be understated and warm
- Buttons should be subtle, with a stronger primary only where truly needed

Status:
- Use small editorial pills
- Status should read clearly without dominating the layout

### Motion

- Motion should be restrained
- Use small fades and slight lift on hover
- Avoid flashy dashboard animation

### What To Avoid

- No generic enterprise blue dashboard styling
- No glossy card UI
- No purple-on-white AI aesthetic
- No overly playful rounded toy look
- No fintech gradient branding language

## Theme 02: Figma

### Direction

Use the same operational workspace language as `paper`, but reinterpret it through a Figma-like interface language.

This theme should feel:

- calm
- precise
- collaborative
- playful in detail, but professional overall
- more interface-driven than editorial

The interface should still read like a workspace, but one with a clearer tool-like identity.

### Visual Principles

- Keep the interface layer mostly black, white, and neutral.
- Allow color to appear in gradients, hero surfaces, and small showcase moments instead of everyday controls.
- Use stronger structural clarity than `paper`, but avoid looking like a finance dashboard.
- Prefer crisp edges, smaller radii, and tighter chrome.
- Make focus treatment more explicit and tool-like.

### Color Language

- Background: soft off-white canvas with subtle colorful ambient gradients
- Surface: neutral white and light-gray panels
- Text: near-black interface text
- Secondary text: medium neutral gray
- Border: clear but quiet monochrome line
- Accent: neutral black for controls; bright color belongs mostly to decorative gradients
- Success/Danger: restrained product states, not playful alerts

### Typography

- Reduce the literary feel compared with `paper`.
- Favor clean sans serif typography for both headings and body copy.
- Use mono selectively for labels, table heads, and micro metadata.
- Preserve high readability for dense operational tables and forms.

### Component Feel

Navigation:
- More structured and interface-like than `paper`
- Active state should be clearer, but still monochrome
- No glossy or neon tool styling

Tables:
- Strong monochrome header contrast
- Clear row separation
- Should feel like a precise collaborative record grid

Forms:
- Inputs should feel crisp, quiet, and neutral
- Focus states should use a dashed, tool-like outline
- Primary buttons should feel compact, pill-based, and grounded in black/white UI

Status:
- Use restrained pills or chips
- States must read clearly without becoming loud

### Motion

- Motion remains subtle
- Favor crisp fades and direct hover emphasis over floaty movement
- Avoid animated flourish

### What To Avoid

- No dark mode look
- No rainbow-everywhere interface
- No glossy glassmorphism
- No consumer-fintech gradients
- No aggressive enterprise dashboard blue
- No marketing-site color saturation in tables, forms, or nav

## Layout Rules

- The app shell should feel like a stable left-navigation workspace.
- Content pages should start with:
  - eyebrow
  - title
  - short explanation
- Keep cards roomy and readable.
- Avoid overly dense control bars; filters should feel deliberate and operational.

These layout rules are shared by both themes and must not diverge across presets.
