// app.js - Dashboard / Home page logic

const CATEGORY_ICONS = {
    'Animals': '🐾',
    'Food': '🍎',
    'Technology': '💻',
    'Nature': '🌿',
    'Emotions': '😊',
    'Travel': '✈️',
};

const CATEGORY_COLORS = [
    'bg-indigo-100 text-indigo-700',
    'bg-green-100 text-green-700',
    'bg-yellow-100 text-yellow-700',
    'bg-pink-100 text-pink-700',
    'bg-blue-100 text-blue-700',
    'bg-orange-100 text-orange-700',
    'bg-purple-100 text-purple-700',
    'bg-teal-100 text-teal-700',
];

async function loadDashboard() {
    await Promise.all([
        loadStats(),
        loadWordOfDay(),
        loadReviewWords(),
        loadCategories(),
    ]);
}

async function loadStats() {
    try {
        const [wordCount, progressStats] = await Promise.all([
            fetch('/api/words/count').then(r => r.json()),
            fetch('/api/progress/stats').then(r => r.json()),
        ]);
        document.getElementById('totalWords').textContent = wordCount.count ?? 0;
        document.getElementById('masteredWords').textContent = progressStats.mastered ?? 0;
        document.getElementById('learningWords').textContent = progressStats.learning ?? 0;
        document.getElementById('accuracy').textContent =
            (progressStats.accuracy !== undefined ? progressStats.accuracy.toFixed(1) : '0') + '%';
    } catch (e) {
        console.error('Failed to load stats', e);
    }
}

async function loadWordOfDay() {
    try {
        const words = await fetch('/api/words/random?limit=1').then(r => r.json());
        if (!words || words.length === 0) return;
        const w = words[0];
        document.getElementById('wod-word').textContent = w.word;
        document.getElementById('wod-pronunciation').textContent = w.pronunciation || '';
        document.getElementById('wod-translation').textContent = w.translation;
        document.getElementById('wod-example').textContent = w.example ? `"${w.example}"` : '';
    } catch (e) {
        console.error('Failed to load word of day', e);
    }
}

async function loadReviewWords() {
    try {
        const reviewList = await fetch('/api/progress/review').then(r => r.json());
        const container = document.getElementById('reviewList');
        const noReview = document.getElementById('noReview');

        if (!reviewList || reviewList.length === 0) {
            container.classList.add('hidden');
            noReview.classList.remove('hidden');
            return;
        }

        container.innerHTML = reviewList.slice(0, 5).map(p => `
            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-xl">
                <div>
                    <span class="font-semibold text-gray-800">${escapeHtml(p.word.word)}</span>
                    <span class="text-gray-400 text-sm ml-2">${escapeHtml(p.word.translation)}</span>
                </div>
                <span class="badge-mastery-${p.mastery}">${masteryLabel(p.mastery)}</span>
            </div>
        `).join('');

        if (reviewList.length > 5) {
            container.innerHTML += `
                <a href="/learn" class="block text-center text-indigo-500 hover:underline text-sm py-2">
                    +${reviewList.length - 5} từ nữa cần ôn tập →
                </a>`;
        }
    } catch (e) {
        console.error('Failed to load review words', e);
    }
}

async function loadCategories() {
    try {
        const categories = await fetch('/api/words/categories').then(r => r.json());
        const grid = document.getElementById('categoriesGrid');
        if (!categories || categories.length === 0) return;
        grid.innerHTML = categories.map((cat, i) => {
            const icon = CATEGORY_ICONS[cat] || '📝';
            const color = CATEGORY_COLORS[i % CATEGORY_COLORS.length];
            return `
                <a href="/vocabulary?category=${encodeURIComponent(cat)}"
                   class="bg-white rounded-xl shadow-sm p-4 text-center hover:shadow-md transition-all group">
                    <div class="text-3xl mb-2">${icon}</div>
                    <p class="font-semibold text-gray-700 text-sm group-hover:text-indigo-600 transition-colors">${escapeHtml(cat)}</p>
                </a>`;
        }).join('');
    } catch (e) {
        console.error('Failed to load categories', e);
    }
}

function masteryLabel(mastery) {
    const labels = { NEW: 'Mới', LEARNING: 'Đang học', REVIEWING: 'Ôn tập', MASTERED: 'Đã thuộc' };
    return labels[mastery] || mastery;
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Initialize
loadDashboard();
