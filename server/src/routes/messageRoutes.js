// src/routes/messageRoutes.js
const express = require('express');
const router = express.Router();
const messageController = require('../controllers/messageController');

// Định nghĩa API: GET /api/messages/:roomId
router.get('/search', messageController.searchMessages);
router.get('/:roomId', messageController.getMessages);

module.exports = router;