const fs = require('fs');

const files = ['frontend/app.js', 'app.js', 'frontend/index.html', 'index.html'];

files.forEach(file => {
    if (fs.existsSync(file)) {
        let content = fs.readFileSync(file, 'utf8');
        // Replace all ?{ with ${
        const fixed = content.replace(/\?\{/g, '${');
        fs.writeFileSync(file, fixed, 'utf8');
        console.log(`Fixed template literals in ${file}`);
    }
});
