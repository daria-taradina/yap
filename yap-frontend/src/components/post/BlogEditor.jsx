import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Placeholder from '@tiptap/extension-placeholder';
import styles from './BlogEditor.module.css';

/**
 * BlogEditor
 *
 * TipTap rich text editor for blog posts.
 * Outputs JSON (TipTap format) stored as content in the posts table.
 *
 * Props:
 *   onChange {fn(json)}  — called with TipTap JSON on every change
 *   placeholder {string}
 */
export default function BlogEditor({ onChange, placeholder = 'Write your piece...' }) {
  const editor = useEditor({
    extensions: [
      StarterKit,
      Placeholder.configure({ placeholder }),
    ],
    editorProps: {
      attributes: { class: styles.editor },
    },
    onUpdate({ editor }) {
      onChange?.(editor.getJSON());
    },
  });

  if (!editor) return null;

  return (
    <div className={styles.editorWrap}>
      {/* Toolbar */}
      <div className={styles.toolbar}>
        <ToolBtn
          label="H1"
          active={editor.isActive('heading', { level: 1 })}
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          text="H1"
        />
        <ToolBtn
          label="H2"
          active={editor.isActive('heading', { level: 2 })}
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          text="H2"
        />

        <div className={styles.divider} />

        <ToolBtn
          label="Bold"
          active={editor.isActive('bold')}
          onClick={() => editor.chain().focus().toggleBold().run()}
          icon="ti ti-bold"
        />
        <ToolBtn
          label="Italic"
          active={editor.isActive('italic')}
          onClick={() => editor.chain().focus().toggleItalic().run()}
          icon="ti ti-italic"
        />

        <div className={styles.divider} />

        <ToolBtn
          label="Bullet list"
          active={editor.isActive('bulletList')}
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          icon="ti ti-list"
        />
        <ToolBtn
          label="Ordered list"
          active={editor.isActive('orderedList')}
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          icon="ti ti-list-numbers"
        />
        <ToolBtn
          label="Blockquote"
          active={editor.isActive('blockquote')}
          onClick={() => editor.chain().focus().toggleBlockquote().run()}
          icon="ti ti-quote"
        />

        <div className={styles.divider} />

        <ToolBtn
          label="Divider"
          onClick={() => editor.chain().focus().setHorizontalRule().run()}
          icon="ti ti-separator"
        />
        <ToolBtn
          label="Undo"
          onClick={() => editor.chain().focus().undo().run()}
          icon="ti ti-arrow-back-up"
        />
        <ToolBtn
          label="Redo"
          onClick={() => editor.chain().focus().redo().run()}
          icon="ti ti-arrow-forward-up"
        />
      </div>

      {/* Editor */}
      <EditorContent editor={editor} />
    </div>
  );
}

function ToolBtn({ label, active, onClick, icon, text }) {
  return (
    <button
      type="button"
      className={`${styles.toolBtn} ${active ? styles.active : ''}`}
      onClick={onClick}
      title={label}
      aria-label={label}
    >
      {icon ? <i className={icon} aria-hidden="true" /> : text}
    </button>
  );
}
