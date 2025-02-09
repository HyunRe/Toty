// 예시 데이터 생성 (JSON 형태)
const userInfoResponse = {
  id: 1,
  nickname: "홍길동",
  status_message: "안녕하세요! 개발자 홍길동입니다.",
  tags: ["Java", "Spring", "React", "AWS"],
  profileImgUrl: "https://via.placeholder.com/120",
  links: [
      { site: "Github", url: "https://github.com/honggildong" },
      { site: "Blog", url: "https://honggildong.dev" }
  ],
  followerCount: 123,
  followingCount: 45,
  isFollowing: false,
  role: "MENTOR",
};

document.addEventListener("DOMContentLoaded", () => {
  mapUserInfo(userInfoResponse);
});

// HTML에 데이터 매핑
function mapUserInfo(userInfo) {
  document.getElementById("profile-img").src = userInfo.profileImgUrl;
  document.getElementById("nickname").textContent = userInfo.nickname;
  document.getElementById("status-message").textContent = userInfo.status_message;
  document.getElementById("followerCount").textContent = `Follower: ${userInfo.followerCount}`;
  document.getElementById("followingCount").textContent = `Following: ${userInfo.followingCount}`;
  
  // 팔로우 버튼 상태 설정
  const followBtn = document.getElementById("followBtn");
  if (userInfo.isFollowing) {
    followBtn.classList.add("following"); // 둘 중 하나 따라 버튼에 다른 css
    followBtn.classList.remove("unfollowing");
  } else {
    followBtn.classList.add("unfollowing");
    followBtn.classList.remove("following");
  }
  
  // 뱃지
  const mentorBadge = document.getElementById("mentorBadge");
  if (userInfo.role === "MENTOR") {
      mentorBadge.style.display = "inline-block"; // 또는 기본값 "inline" 등
  } else {
      mentorBadge.style.display = "none";
  }

  // 태그 매핑
  const tagsContainer = document.getElementById("tags-container");
  console.log("tagsContainer 찾기:", tagsContainer);
  if (!tagsContainer) {
      console.error("tagsContainer를 찾을 수 없습니다.");
      return;
  }
  userInfo.tags.forEach(tag => {
      const tagElement = document.createElement("div");
      tagElement.className = 'tag';
      tagElement.textContent = `#${tag}`;
      tagsContainer.appendChild(tagElement);
  });
  
  // 링크 매핑
  const linksContainer = document.getElementById("links-container");
  userInfo.links.forEach(link => {
      const linkElement = document.createElement("a");
      linkElement.className = "link-item";
      linkElement.href = link.url;
      linkElement.target = "_blank";

      let siteImgSrc = ""
      if (link.site === "GITHUB") {
        siteImgSrc = "../../img/github-logo.png" // todo 경로 확인
      } else {
        siteImgSrc = "../../img/blog-logo.png" 
      }

      linkElement.innerHTML = `<img src="${siteImgSrc}" alt="${link.site} 로고"> <span>"${link.url}"</span>`; //경로 설정?
      linksContainer.appendChild(linkElement);
  });
}

// 팔로우 버튼 누르면 데이터 전송
// 해당 버튼 클래스가 following -> post /api/users/ json id:userInfo.id 요청 보내고 class unfollow 바꾸기 / unfollowing ->
followBtn.addEventListener('click', function() {
const userId = userInfo.id;
const currentState = userInfo.isFollowing;
// 현재 상태 반대 수행
$.ajax({
      url: currentState ? `/api/follow/${userId}` : '/api/follow/',
      type: currentState ? 'DELETE' : 'POST',
      data: currentState ?  null : JSON.stringify({ id: toId }),
      dataType: 'json',
      success: function (response) {
        if (response.status === 'error') {
          Swal.fire({
            icon: 'warning',
            text: response.errorMessage,
            ...swalConfig,
          });
        }
        if (response.status === 'success') {
          if (isFollowing) {
              followBtn.classList.remove('following');
              followBtn.classList.add('unfollow');
          } else {
              followBtn.classList.remove('unfollow');
              followBtn.classList.add('following');
          }
          userInfo.isFollowing = !currentState;
          userInfo.followerCount += 1;
        }
      },
      error: function () {
        Swal.fire({
          icon: 'error',
          text: '서버 응답이 올바르지 않습니다.',
          ...swalConfig,
        });
      },
    });
})



// 팔로워 버튼 누르면 페이지 보이기 
const showFollowersBtn = document.getElementById('followerCount');
if (showFollowersBtn) {
showFollowersBtn.addEventListener('click', function () {
      const options = 'width=700, height=600, top=50, left=50, scrollbars=yes'
      const userId = userInfo.id;
      const url = `/api/follow/${userId}/followers`;
      window.open(url,'_blank',options)
  });
  }


// 팔로잉 버튼 누르면 페이지 보이기
const showFollowingsBtn = document.getElementById('followingCount');
  if (showFollowingsBtn) {
    showFollowingsBtn.addEventListener('click', function () {
      const options = 'width=700, height=600, top=50, left=50, scrollbars=yes'
      const userId = userData.id;
      const url = `/api/follow/${userId}/followings`;
      window.open(url,'_blank',options)
  });
  }

