document.addEventListener("DOMContentLoaded", function () {
    function setActiveMenu(menuId) {
        document.getElementById("profileMenu").classList.remove("active-menu");
        document.getElementById("myPostsMenu").classList.remove("active-menu");
        document.getElementById("savedPostsMenu").classList.remove("active-menu");
        
        document.getElementById(menuId).classList.add("active-menu");
    }
    
    document.getElementById("profileMenu").addEventListener("click", function () {
        setActiveMenu("profileMenu");
    });
    
    document.getElementById("myPostsMenu").addEventListener("click", function () {
        setActiveMenu("myPostsMenu");
    });
    
    document.getElementById("savedPostsMenu").addEventListener("click", function () {
        setActiveMenu("savedPostsMenu");
    });
});
