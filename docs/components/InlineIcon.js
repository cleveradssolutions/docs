export default function InlineIcon({ src, text }) {
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
      <img
        src={`https://raw.githubusercontent.com/cleveradssolutions/docs/main/docs${src}`}
        alt={text}
        width="21"
        height="21"
        style={{ verticalAlign: 'middle' }}
      />
      <b>{text}</b>
    </span>
  );
}