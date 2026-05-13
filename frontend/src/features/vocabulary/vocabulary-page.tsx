"use client";

import { useSearchParams } from "next/navigation";
import {
  IconBooks,
  IconEdit,
  IconPlus,
  IconSearch,
  IconSparkles,
  IconTrash
} from "@tabler/icons-react";
import { FormEvent, useEffect, useMemo, useState } from "react";
import { AppShell } from "@/components/layout/app-shell";
import { CatLoader } from "@/components/ui/cat-loader";
import { Dialog } from "@/components/ui/dialog";
import { Select } from "@/components/ui/select";
import { useToast } from "@/components/ui/toast";
import { api } from "@/lib/api";
import type { DifficultyLevel, Word } from "@/types/api";

const emptyWord: Partial<Word> = {
  word: "",
  translation: "",
  pronunciation: "",
  category: "",
  difficulty: "INTERMEDIATE",
  example: ""
};

function DifficultyPill({ difficulty }: { difficulty?: string }) {
  if (!difficulty) return null;
  const styles: Record<string, string> = {
    BEGINNER: "bg-accent text-accent-foreground",
    INTERMEDIATE: "bg-secondary/40 text-secondary-foreground",
    ADVANCED: "bg-destructive/15 text-destructive"
  };
  const labels: Record<string, string> = {
    BEGINNER: "Beginner",
    INTERMEDIATE: "Intermediate",
    ADVANCED: "Advanced"
  };
  return (
    <span
      className={`shrink-0 rounded-full px-2 py-0.5 font-display text-[0.7rem] font-bold uppercase tracking-[0.04em] ${styles[difficulty] ?? "bg-muted text-muted-foreground"}`}
    >
      {labels[difficulty] ?? difficulty}
    </span>
  );
}

function EnrichmentPill({ status }: { status?: string }) {
  const s = status ?? "NOT_REQUESTED";
  const conf: Record<string, { cls: string; label: string }> = {
    NOT_REQUESTED: { cls: "bg-muted text-muted-foreground border border-border", label: "Not enriched" },
    PENDING: { cls: "bg-secondary/40 text-secondary-foreground border border-secondary/60", label: "Enriching..." },
    RUNNING: { cls: "bg-secondary/40 text-secondary-foreground border border-secondary/60", label: "Enriching..." },
    PARTIAL: { cls: "bg-accent text-accent-foreground border border-accent/60", label: "Partial" },
    COMPLETED: { cls: "bg-emerald-100 text-emerald-800 border border-emerald-300", label: "Enriched" },
    FAILED: { cls: "bg-destructive/10 text-destructive border border-destructive/30", label: "Failed" }
  };
  const { cls, label } = conf[s] ?? conf.NOT_REQUESTED;
  return (
    <span className={`rounded-full px-2 py-0.5 font-display text-[0.68rem] font-bold uppercase ${cls}`}>
      {label}
    </span>
  );
}

export function VocabularyPage() {
  const params = useSearchParams();
  const [words, setWords] = useState<Word[]>([]);
  const [categories, setCategories] = useState<string[]>([]);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState(params.get("category") || "");
  const [difficulty, setDifficulty] = useState("");
  const [topic, setTopic] = useState("");
  const [cefr, setCefr] = useState("");
  const [editing, setEditing] = useState<Partial<Word> | null>(null);
  const [deleting, setDeleting] = useState<Word | null>(null);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  async function load() {
    const [allWords, cats] = await Promise.all([
      api.words().catch(() => [] as Word[]),
      api.categories().catch(() => [] as string[])
    ]);
    setWords(allWords);
    setCategories(cats);
    setLoading(false);
  }

  useEffect(() => {
    load().catch(() =>
      toast("Không thể tải dữ liệu từ backend. Đang hiển thị danh sách trống.", "warning")
    );
  }, [toast]);

  const topics = useMemo(() => {
    const seen = new Set<string>();
    return words
      .filter((w) => w.topic?.name && !seen.has(w.topic.name) && seen.add(w.topic.name))
      .map((w) => w.topic!.name);
  }, [words]);

  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    const seen = new Set<string>();
    return words.filter((word) => {
      const key = word.word.toLowerCase();
      if (seen.has(key)) return false;
      const matchesSearch =
        !q ||
        word.word.toLowerCase().includes(q) ||
        (word.translation || "").toLowerCase().includes(q);
      const matchesCategory = !category || word.category === category;
      const matchesDifficulty = !difficulty || word.difficulty === difficulty;
      const matchesTopic = !topic || word.topic?.name === topic;
      const matchesCefr = !cefr || word.cefrLevel === cefr;
      const passes = matchesSearch && matchesCategory && matchesDifficulty && matchesTopic && matchesCefr;
      if (passes) seen.add(key);
      return passes;
    });
  }, [words, search, category, difficulty, topic, cefr]);

  async function saveWord(event: FormEvent) {
    event.preventDefault();
    if (!editing?.word) return;
    try {
      if (editing.id) {
        await api.updateWord(editing.id, editing);
        toast("Đã cập nhật từ vựng.", "success");
      } else {
        await api.createWord(editing);
        toast("Đã thêm từ mới.", "success");
      }
      setEditing(null);
      await load();
    } catch {
      toast("Không thể lưu từ vựng. Vui lòng thử lại.", "error");
    }
  }

  async function deleteWord() {
    if (!deleting) return;
    try {
      await api.deleteWord(deleting.id);
      setDeleting(null);
      toast("Đã xóa từ vựng.", "success");
      await load();
    } catch {
      toast("Không thể xóa từ vựng. Vui lòng thử lại.", "error");
    }
  }

  async function enrich(id: number) {
    try {
      await api.enrichWord(id);
      toast("Enrichment queued.", "success");
      await load();
    } catch {
      toast("Không thể enrich từ này.", "error");
    }
  }

  return (
    <AppShell>
      {/* Hero Section */}
      <div className="-mx-4 -mt-6 lg:-mt-8 xl:-mx-12 bg-primary px-4 py-6 xl:px-12">
        <div className="flex flex-col gap-4 md:flex-row md:items-center">
          <div className="flex-1">
            <h2 className="font-display text-[32px] font-black text-primary-foreground">My Vocabulary</h2>
            <p className="mt-1 font-body text-[17px] text-accent">
              {words.length ? `${filtered.length} từ` : "Loading words..."}
            </p>
          </div>
          <div className="flex gap-3">
            <label className="relative flex-1 md:w-72">
              <IconSearch className="absolute left-3 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
              <input
                type="search"
                placeholder="Search words..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="h-11 w-full rounded-full border-2 border-transparent bg-white pl-10 pr-4 font-body text-[17px] font-medium text-foreground outline-none placeholder:text-muted-foreground/60 focus:border-primary"
              />
            </label>
            <button
              type="button"
              onClick={() => setEditing(emptyWord)}
              className="btn-press flex items-center gap-2 whitespace-nowrap rounded-full border-2 border-white bg-white px-5 font-display text-[15px] font-bold uppercase tracking-[0.05em] text-primary"
            >
              <IconPlus className="h-5 w-5" /> Add Word
            </button>
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="-mx-4 mb-6 flex flex-nowrap gap-3 overflow-x-auto border-b-2 border-border bg-white px-4 py-4 xl:-mx-12 xl:px-12">
        <Select
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          aria-label="Filter by category"
          className="w-44"
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat} value={cat}>{cat}</option>
          ))}
        </Select>
        <Select
          value={difficulty}
          onChange={(e) => setDifficulty(e.target.value)}
          aria-label="Filter by level"
          className="w-44"
        >
          <option value="">All Levels</option>
          <option value="BEGINNER">Beginner</option>
          <option value="INTERMEDIATE">Intermediate</option>
          <option value="ADVANCED">Advanced</option>
        </Select>
        <Select
          value={topic}
          onChange={(e) => setTopic(e.target.value)}
          aria-label="Filter by topic"
          className="w-44"
        >
          <option value="">All Topics</option>
          {topics.map((t) => (
            <option key={t} value={t}>{t}</option>
          ))}
        </Select>
        <Select
          value={cefr}
          onChange={(e) => setCefr(e.target.value)}
          aria-label="Filter by CEFR level"
          className="w-40"
        >
          <option value="">All CEFR</option>
          {["A1", "A2", "B1", "B2", "C1", "C2"].map((level) => (
            <option key={level} value={level}>{level}</option>
          ))}
        </Select>
      </div>

      {/* Empty State */}
      {!filtered.length ? (
        <div className="flex flex-col items-center justify-center gap-5 py-16 text-muted-foreground">
          <IconBooks className="h-16 w-16" strokeWidth={1.5} />
          <p className="font-display text-2xl font-bold text-foreground">No words found</p>
          <p className="font-body text-[17px]">Try adjusting your filters or add new words.</p>
          <button
            type="button"
            onClick={() => setEditing(emptyWord)}
            className="btn-press flex items-center gap-2 rounded-xl bg-primary px-6 py-3 font-display text-[15px] font-bold uppercase tracking-[0.02em] text-primary-foreground"
          >
            <IconPlus className="h-5 w-5" /> Add First Word
          </button>
        </div>
      ) : null}

      {/* Word Grid */}
      {filtered.length ? (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filtered.map((word) => (
            <div
              key={word.id}
              className="flex flex-col gap-2 rounded-xl border-2 border-border bg-white p-5 transition-[border-color,box-shadow] hover:border-primary hover:shadow-[0_2px_8px_rgba(0,101,144,0.12)]"
            >
              {word.imageUrl ? (
                <img
                  src={word.imageUrl}
                  alt={word.word}
                  loading="lazy"
                  className="aspect-video w-full rounded-lg border border-border bg-muted object-cover"
                />
              ) : null}

              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <h3 className="truncate font-display text-[1.1rem] font-bold text-foreground">
                    {word.word}
                  </h3>
                  {word.pronunciation ? (
                    <p className="font-mono text-[0.78rem] text-primary">{word.pronunciation}</p>
                  ) : null}
                </div>
                <DifficultyPill difficulty={word.difficulty} />
              </div>

              <p className="font-body text-[0.9rem] font-semibold text-muted-foreground">
                {word.translation}
              </p>

              {word.example ? (
                <p className="line-clamp-2 font-body text-[0.8rem] italic text-muted-foreground/70">
                  &ldquo;{word.example}&rdquo;
                </p>
              ) : null}

              <div className="flex flex-wrap gap-1">
                {word.category ? (
                  <span className="rounded-full border border-border bg-muted px-2 py-0.5 font-display text-[0.68rem] font-semibold text-muted-foreground">
                    {word.category}
                  </span>
                ) : null}
                {word.topic ? (
                  <span className="rounded-full bg-accent px-2 py-0.5 font-display text-[0.68rem] font-semibold text-accent-foreground">
                    {word.topic.name}
                  </span>
                ) : null}
                {word.cefrLevel ? (
                  <span className="rounded-full bg-secondary/40 px-2 py-0.5 font-display text-[0.68rem] font-semibold text-secondary-foreground">
                    {word.cefrLevel}
                  </span>
                ) : null}
                {word.partOfSpeech ? (
                  <span className="rounded-full bg-muted px-2 py-0.5 font-display text-[0.68rem] text-muted-foreground/70">
                    {word.partOfSpeech}
                  </span>
                ) : null}
                <EnrichmentPill status={word.enrichmentStatus} />
              </div>

              <div className="mt-auto flex gap-2 border-t border-border pt-3">
                <button
                  type="button"
                  onClick={() => setEditing(word)}
                  className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border-2 border-border bg-white px-3 py-2 font-display text-[0.75rem] font-bold uppercase tracking-[0.05em] text-primary transition hover:bg-accent"
                >
                  <IconEdit className="h-[15px] w-[15px]" /> Edit
                </button>
                {!word.enrichmentStatus ||
                word.enrichmentStatus === "NOT_REQUESTED" ||
                word.enrichmentStatus === "FAILED" ? (
                  <button
                    type="button"
                    onClick={() => enrich(word.id)}
                    className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border-2 border-border bg-white px-3 py-2 font-display text-[0.75rem] font-bold uppercase tracking-[0.05em] text-secondary-foreground transition hover:bg-secondary/30"
                  >
                    <IconSparkles className="h-[15px] w-[15px]" /> Enrich
                  </button>
                ) : null}
                <button
                  type="button"
                  onClick={() => setDeleting(word)}
                  className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border-2 border-border bg-white px-3 py-2 font-display text-[0.75rem] font-bold uppercase tracking-[0.05em] text-destructive transition hover:bg-destructive/10"
                >
                  <IconTrash className="h-[15px] w-[15px]" /> Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : null}

      {/* Add / Edit Modal */}
      <Dialog
        open={!!editing}
        title={editing?.id ? "Edit Word" : "Add New Word"}
        onClose={() => setEditing(null)}
        className="max-w-lg"
      >
        <form className="flex flex-col gap-5" onSubmit={saveWord}>
          <div className="grid grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
                Word *
              </label>
              <input
                required
                placeholder="apple"
                value={editing?.word || ""}
                onChange={(e) => setEditing({ ...editing, word: e.target.value })}
                className="rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
                Translation
              </label>
              <input
                placeholder="Auto-enriched after save"
                value={editing?.translation || ""}
                onChange={(e) => setEditing({ ...editing, translation: e.target.value })}
                className="rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex flex-col gap-1.5">
              <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
                Pronunciation
              </label>
              <input
                placeholder="/ˈæp.əl/"
                value={editing?.pronunciation || ""}
                onChange={(e) => setEditing({ ...editing, pronunciation: e.target.value })}
                className="rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
                Category
              </label>
              <input
                placeholder="Food"
                value={editing?.category || ""}
                onChange={(e) => setEditing({ ...editing, category: e.target.value })}
                className="rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
              />
            </div>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
              Difficulty
            </label>
            <select
              value={editing?.difficulty || "INTERMEDIATE"}
              onChange={(e) => setEditing({ ...editing, difficulty: e.target.value as DifficultyLevel })}
              className="cursor-pointer rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
            >
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </div>

          <div className="flex flex-col gap-1.5">
            <label className="font-display text-[13px] font-bold uppercase tracking-widest text-muted-foreground">
              Example Sentence
            </label>
            <input
              placeholder="I eat an apple every day."
              value={editing?.example || ""}
              onChange={(e) => setEditing({ ...editing, example: e.target.value })}
              className="rounded-xl border-2 border-border bg-muted px-3 py-2.5 font-body text-[17px] text-foreground outline-none focus:border-primary"
            />
          </div>

          <div className="flex justify-end gap-3 pt-1">
            <button
              type="button"
              onClick={() => setEditing(null)}
              className="rounded-xl border-2 border-border px-6 py-2.5 font-display text-[15px] font-bold uppercase tracking-[0.02em] text-muted-foreground transition hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-press rounded-xl bg-primary px-6 py-2.5 font-display text-[15px] font-bold uppercase tracking-[0.02em] text-primary-foreground"
            >
              Save Word
            </button>
          </div>
        </form>
      </Dialog>

      {/* Delete Modal */}
      <Dialog
        open={!!deleting}
        title="Delete Word?"
        onClose={() => setDeleting(null)}
        className="max-w-sm"
      >
        <div className="flex flex-col gap-5">
          <div className="flex items-center gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-full bg-destructive/15">
              <IconTrash className="h-5 w-5 text-destructive" />
            </div>
            <p className="font-body text-[17px] text-muted-foreground">
              Delete <strong className="text-foreground">{deleting?.word}</strong>? This action
              cannot be undone.
            </p>
          </div>
          <div className="flex justify-end gap-3">
            <button
              type="button"
              onClick={() => setDeleting(null)}
              className="rounded-xl border-2 border-border px-6 py-2.5 font-display text-[15px] font-bold uppercase tracking-[0.02em] text-muted-foreground transition hover:bg-muted"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={deleteWord}
              className="btn-press-error rounded-xl bg-destructive px-6 py-2.5 font-display text-[15px] font-bold uppercase tracking-[0.02em] text-white"
            >
              Delete
            </button>
          </div>
        </div>
      </Dialog>
    </AppShell>
  );
}
