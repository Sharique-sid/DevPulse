type NoticeProps = {
  tone: "success" | "error" | "info";
  text: string;
};

export function Notice({ tone, text }: NoticeProps) {
  return <div className={`notice notice-${tone}`}>{text}</div>;
}
