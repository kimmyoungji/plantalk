// DOM 요소 참조
const messageForm = document.getElementById('messageForm');
const messageInput = document.getElementById('content');
const messageArea = document.getElementById('messageArea');
const plantIdInput = document.getElementById('plantId');

// 현재 선택된 식물 ID
const currentPlantId = plantIdInput ? plantIdInput.value : null;

// 페이지 로드 시 스크롤을 가장 아래로 이동
window.onload = function() {
    scrollToBottom();
};

// 메시지 전송 폼 제출 이벤트 처리
if (messageForm) {
    messageForm.addEventListener('submit', function(event) {
        event.preventDefault();
        
        if (!currentPlantId) {
            alert('먼저 식물을 선택해주세요.');
            return;
        }
        
        const messageContent = messageInput.value.trim();
        if (messageContent) {
            sendMessage(messageContent);
        }
    });
}

// 메시지 전송 함수
function sendMessage(content) {
    const message = {
        plantId: currentPlantId,
        senderType: 'user',
        content: content
    };
    
    fetch('/api/message', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(message)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('메시지 전송에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        if (data.success) {
            // 메시지 전송 성공 시 입력 필드 초기화
            messageInput.value = '';
            
            // 새 메시지 추가 및 화면 갱신
            addMessage(data.data);
            
            // 자동 응답 요청 (식물 메시지 생성)
            generatePlantResponse();
        } else {
            console.error('메시지 전송 오류:', data.message);
            alert('메시지 전송 오류: ' + data.message);
        }
    })
    .catch(error => {
        console.error('메시지 전송 오류:', error);
        alert('메시지 전송 오류: ' + error.message);
    });
}

// 식물 자동 응답 생성 함수
function generatePlantResponse() {
    // 가장 최근 상태 ID 가져오기
    fetch(`/api/state/plant/${currentPlantId}/latest`)
        .then(response => {
            if (!response.ok) {
                throw new Error('최근 상태 정보를 가져오는데 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            if (data.success && data.data) {
                const stateId = data.data.stateId;
                
                // 자동 메시지 생성 요청
                return fetch(`/api/message/plant/${currentPlantId}/state/${stateId}/generate`, {
                    method: 'POST'
                });
            } else {
                throw new Error('상태 정보가 없습니다.');
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('자동 응답 생성에 실패했습니다.');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                // 식물 응답 메시지 추가
                addMessage(data.data);
            } else {
                console.error('자동 응답 생성 오류:', data.message);
            }
        })
        .catch(error => {
            console.error('자동 응답 생성 오류:', error);
        });
}

// 메시지 화면에 추가 함수
function addMessage(message) {
    const messageElement = document.createElement('div');
    messageElement.className = message.senderType === 'plant' ? 'message plant-message' : 'message user-message';
    
    const contentElement = document.createElement('div');
    contentElement.className = 'message-content';
    contentElement.textContent = message.content;
    
    const timeElement = document.createElement('div');
    timeElement.className = 'message-time';
    
    // 날짜 포맷팅
    const date = new Date(message.createdAt);
    timeElement.textContent = formatDate(date);
    
    messageElement.appendChild(contentElement);
    messageElement.appendChild(timeElement);
    
    messageArea.appendChild(messageElement);
    
    // 스크롤을 가장 아래로 이동
    scrollToBottom();
}

// 날짜 포맷팅 함수
function formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 스크롤을 가장 아래로 이동하는 함수
function scrollToBottom() {
    if (messageArea) {
        messageArea.scrollTop = messageArea.scrollHeight;
    }
}

// 주기적으로 메시지 목록 갱신 (선택적)
let lastMessageId = 0;

function updateMessages() {
    if (!currentPlantId) return;
    
    fetch(`/api/message/plant/${currentPlantId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data && data.data.length > 0) {
                const messages = data.data;
                const latestMessageId = Math.max(...messages.map(m => m.messageId));
                
                if (latestMessageId > lastMessageId) {
                    // 새 메시지가 있는 경우
                    const newMessages = messages.filter(m => m.messageId > lastMessageId);
                    
                    newMessages.forEach(message => {
                        addMessage(message);
                    });
                    
                    lastMessageId = latestMessageId;
                }
            }
        })
        .catch(error => {
            console.error('메시지 갱신 오류:', error);
        });
}

// 초기 메시지 ID 설정 및 주기적 갱신 시작
function initializeChat() {
    if (!currentPlantId) return;
    
    fetch(`/api/message/plant/${currentPlantId}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data && data.data.length > 0) {
                const messages = data.data;
                lastMessageId = Math.max(...messages.map(m => m.messageId));
                
                // 주기적 갱신 시작 (10초마다)
                setInterval(updateMessages, 10000);
            }
        })
        .catch(error => {
            console.error('초기화 오류:', error);
        });
}

// 채팅 초기화
if (currentPlantId) {
    initializeChat();
}
