-- Sample vocabulary data for the Smart Vocabulary Learning App

INSERT INTO words (word, translation, example, pronunciation, category, difficulty, created_at) VALUES
-- Animals
('cat', 'con mèo', 'The cat is sleeping on the sofa.', '/kæt/', 'Animals', 'BEGINNER', CURRENT_TIMESTAMP),
('dog', 'con chó', 'My dog loves to play fetch.', '/dɒɡ/', 'Animals', 'BEGINNER', CURRENT_TIMESTAMP),
('elephant', 'con voi', 'The elephant is the largest land animal.', '/ˈɛlɪfənt/', 'Animals', 'BEGINNER', CURRENT_TIMESTAMP),
('butterfly', 'con bướm', 'The butterfly landed on a flower.', '/ˈbʌtəflaɪ/', 'Animals', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('cheetah', 'con báo đốm', 'The cheetah is the fastest land animal.', '/ˈtʃiːtə/', 'Animals', 'INTERMEDIATE', CURRENT_TIMESTAMP),

-- Food
('apple', 'quả táo', 'An apple a day keeps the doctor away.', '/ˈæpəl/', 'Food', 'BEGINNER', CURRENT_TIMESTAMP),
('bread', 'bánh mì', 'She baked fresh bread this morning.', '/brɛd/', 'Food', 'BEGINNER', CURRENT_TIMESTAMP),
('noodle', 'mì sợi', 'Vietnamese pho is a famous noodle soup.', '/ˈnuːdəl/', 'Food', 'BEGINNER', CURRENT_TIMESTAMP),
('avocado', 'quả bơ', 'Avocado is rich in healthy fats.', '/ˌævəˈkɑːdoʊ/', 'Food', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('persimmon', 'quả hồng', 'The persimmon tree bears fruit in autumn.', '/pərˈsɪmən/', 'Food', 'ADVANCED', CURRENT_TIMESTAMP),

-- Technology
('computer', 'máy tính', 'She works on her computer every day.', '/kəmˈpjuːtər/', 'Technology', 'BEGINNER', CURRENT_TIMESTAMP),
('software', 'phần mềm', 'He is a software engineer at a tech company.', '/ˈsɒftweər/', 'Technology', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('algorithm', 'thuật toán', 'The search algorithm returns results in milliseconds.', '/ˈælɡərɪðəm/', 'Technology', 'ADVANCED', CURRENT_TIMESTAMP),
('database', 'cơ sở dữ liệu', 'All the user data is stored in a database.', '/ˈdeɪtəbeɪs/', 'Technology', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('cybersecurity', 'an ninh mạng', 'Cybersecurity is crucial in the digital age.', '/ˌsaɪbəsɪˈkjʊərɪti/', 'Technology', 'ADVANCED', CURRENT_TIMESTAMP),

-- Nature
('mountain', 'núi', 'We hiked to the top of the mountain.', '/ˈmaʊntɪn/', 'Nature', 'BEGINNER', CURRENT_TIMESTAMP),
('river', 'sông', 'The river flows through the valley.', '/ˈrɪvər/', 'Nature', 'BEGINNER', CURRENT_TIMESTAMP),
('forest', 'rừng', 'The Amazon is the world''s largest forest.', '/ˈfɒrɪst/', 'Nature', 'BEGINNER', CURRENT_TIMESTAMP),
('waterfall', 'thác nước', 'The waterfall was breathtakingly beautiful.', '/ˈwɔːtərfɔːl/', 'Nature', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('archipelago', 'quần đảo', 'Vietnam has a beautiful archipelago in the East Sea.', '/ˌɑːrkɪˈpɛləɡoʊ/', 'Nature', 'ADVANCED', CURRENT_TIMESTAMP),

-- Emotions
('happy', 'vui vẻ', 'She felt happy when she passed the exam.', '/ˈhæpi/', 'Emotions', 'BEGINNER', CURRENT_TIMESTAMP),
('sad', 'buồn', 'He was sad when his team lost the game.', '/sæd/', 'Emotions', 'BEGINNER', CURRENT_TIMESTAMP),
('excited', 'hào hứng', 'The children were excited about the trip.', '/ɪkˈsaɪtɪd/', 'Emotions', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('melancholy', 'u sầu', 'The rainy day brought a feeling of melancholy.', '/ˈmɛlənkɒli/', 'Emotions', 'ADVANCED', CURRENT_TIMESTAMP),
('nostalgic', 'hoài niệm', 'Looking at old photos made her feel nostalgic.', '/nɒˈstældʒɪk/', 'Emotions', 'ADVANCED', CURRENT_TIMESTAMP),

-- Travel
('passport', 'hộ chiếu', 'Don''t forget your passport when traveling abroad.', '/ˈpɑːspɔːrt/', 'Travel', 'BEGINNER', CURRENT_TIMESTAMP),
('hotel', 'khách sạn', 'We booked a hotel near the beach.', '/hoʊˈtɛl/', 'Travel', 'BEGINNER', CURRENT_TIMESTAMP),
('itinerary', 'lịch trình', 'She planned every detail in her itinerary.', '/aɪˈtɪnəreri/', 'Travel', 'ADVANCED', CURRENT_TIMESTAMP),
('souvenir', 'đồ lưu niệm', 'He bought a souvenir from every city he visited.', '/ˌsuːvəˈnɪər/', 'Travel', 'INTERMEDIATE', CURRENT_TIMESTAMP),
('accommodation', 'chỗ ở', 'They found affordable accommodation near the city center.', '/əˌkɒməˈdeɪʃən/', 'Travel', 'ADVANCED', CURRENT_TIMESTAMP);
