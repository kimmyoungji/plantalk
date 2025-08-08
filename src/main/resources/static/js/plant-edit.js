/**
 * PlantTalk 식물 관리 관련 JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {

    // 슬라이더 값 표시
    const lightLevelSlider = document.getElementById('lightLevel');
    const lightLevelValue = document.getElementById('lightLevelValue');
    const moistureSlider = document.getElementById('moisture');
    const moistureValue = document.getElementById('moistureValue');
    const temperatureSlider = document.getElementById('temperature');
    const temperatureValue = document.getElementById('temperatureValue');
    
    lightLevelSlider.addEventListener('input', function() {
        lightLevelValue.textContent = this.value + '%';
    });
    
    moistureSlider.addEventListener('input', function() {
        moistureValue.textContent = this.value + '%';
    });
    
    temperatureSlider.addEventListener('input', function() {
        temperatureValue.textContent = this.value + '°C';
    });
    
    // 식물 정보 수정 폼 처리
    const plantEditForm = document.getElementById('plantEditForm');
    if (plantEditForm) {
        plantEditForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            
            const plantId = window.location.pathname.split('/').pop();
            const nameInput = document.getElementById('name');
            const speciesInput = document.getElementById('species');
            const userIdInput = document.querySelector('input[name="userId"]');
            
            // JSON 데이터 객체 생성
            const plantData = {
                name: nameInput.value,
                species: speciesInput.value,
                userId: parseInt(userIdInput.value)
            };
            
            try {
                const response = await fetch(`/api/plant/${plantId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(plantData)
                });
                
                if (response.ok) {
                    // 수정 성공
                    showSuccess('식물 정보가 성공적으로 수정되었습니다.');
                    setTimeout(() => {
                        window.location.reload();
                    }, 2000);
                } else {
                    // 수정 실패
                    const errorData = await response.json();
                    showError(errorData.message || '식물 정보 수정에 실패했습니다.');
                }
            } catch (error) {
                console.error('식물 정보 수정 중 오류 발생:', error);
                showError('서버 연결 중 오류가 발생했습니다.');
            }
        });
    }
    
    // 식물 상태 추가 폼 처리
    const updateStateBtn = document.getElementById('updateStateBtn');
    const stateUpdateForm = document.getElementById('stateUpdateForm');
    updateStateBtn.addEventListener('click', async function() {
        const formData = new FormData(stateUpdateForm);
        const plantId = formData.get('plantId');
        const lightLevel = formData.get('lightLevel');
        const moisture = formData.get('moisture');
        const temperature = formData.get('temperature');
        const touched = formData.get('touched') === 'on';
            
        // JSON 데이터로 변환
        const stateData = {
            plantId: plantId,
            lightLevel: parseInt(lightLevel),
            moisture: parseInt(moisture),
            temperature: parseInt(temperature),
            touched: touched
        };
            
        try {
            const response = await fetch('/api/state', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(stateData)
            });
            
            if (response.ok) {
                // 상태 업데이트 성공
                showSuccess('식물 상태가 성공적으로 업데이트되었습니다.');
                setTimeout(() => {
                    window.location.reload();
                }, 2000);
            } else {
                // 상태 업데이트 실패
                const errorData = await response.json();
                showError(errorData.message || '식물 상태 업데이트에 실패했습니다.');
            }
        } catch (error) {
            console.error('식물 상태 업데이트 중 오류 발생:', error);
            showError('서버 연결 중 오류가 발생했습니다.');
        }
    });
    
    // 식물 삭제 폼 처리
    const plantDeleteForm = document.getElementById('plantDeleteForm');
    if (plantDeleteForm) {
        plantDeleteForm.addEventListener('submit', async function(event) {
            event.preventDefault();
            
            if (!confirm('정말로 이 식물을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
                return;
            }
            
            const plantId = window.location.pathname.split('/').pop();
            
            try {
                const response = await fetch(`/api/plant/${plantId}`, {
                    method: 'DELETE'
                });
                
                if (response.ok) {
                    // 삭제 성공
                    showSuccess('식물이 성공적으로 삭제되었습니다.');
                    setTimeout(() => {
                        window.location.href = '/';
                    }, 2000);
                } else {
                    // 삭제 실패
                    const errorData = await response.json();
                    showError(errorData.message || '식물 삭제에 실패했습니다.');
                }
            } catch (error) {
                console.error('식물 삭제 중 오류 발생:', error);
                showError('서버 연결 중 오류가 발생했습니다.');
            }
        });
    }
    
    // 오류 메시지 표시
    function showError(message) {
        const errorDiv = document.createElement('div');
        errorDiv.className = 'alert alert-danger';
        errorDiv.textContent = message;
        
        const form = document.querySelector('form');
        form.insertBefore(errorDiv, form.firstChild);
        
        // 3초 후 메시지 제거
        setTimeout(() => {
            errorDiv.remove();
        }, 3000);
    }
    
    // 성공 메시지 표시
    function showSuccess(message) {
        const successDiv = document.createElement('div');
        successDiv.className = 'alert alert-success';
        successDiv.textContent = message;
        
        const form = document.querySelector('form');
        form.insertBefore(successDiv, form.firstChild);
        
        // 3초 후 메시지 제거
        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }
});
