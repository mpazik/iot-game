const gulp = require('gulp');
const del = require('del');
const gulpSequence = require('gulp-sequence');
const processHtml = require('gulp-processhtml');
const AwsPublish = require('gulp-awspublish');

const distPath = 'dist';


gulp.task('clean', function () {
    return del([distPath]);
});

gulp.task('copy-public', function () {
    return gulp.src(['public/**/*.*', '!public/dev/**/*.*'])
        .pipe(gulp.dest(distPath));
});

gulp.task('copy-libs', function () {
    return gulp.src(['node_modules/pixi-spine/bin/pixi-spine.js'])
        .pipe(gulp.dest(distPath + '/lib'));
});

gulp.task('process-html', function () {
    return gulp.src('public/index.html')
        .pipe(processHtml({}))
        .pipe(gulp.dest(distPath));
});

gulp.task('replace-dev-files', function () {
    return gulp.src(['prod/**/*'])
        .pipe(gulp.dest(distPath));
});

gulp.task('build-prod', gulpSequence('clean', 'copy-public', 'copy-libs', 'process-html', 'replace-dev-files'));

gulp.task('publish', ['build-prod'], function () {
        process.env['AWS_PROFILE'] = 's3_deploy';

        const publisher = AwsPublish.create({
            region: 'eu-central-1',
            params: {
                Bucket: 'game.islesoftales.com'
            }
        });

        // define custom headers
        var headers = {
            // Enable cache when files will be versioned, for now it's only symbolic minute
            'Cache-Control': 'max-age=60, no-transform, public'
        };

        return gulp.src(distPath + '/**/*')
            // gzip, Set Content-Encoding headers
            .pipe(AwsPublish.gzip())

            // publisher will add Content-Length, Content-Type and headers specified above
            // If not specified it will set x-amz-acl to public-read by default
            .pipe(publisher.publish(headers))

            // create a cache file to speed up consecutive uploads
            .pipe(publisher.cache())
            .pipe(AwsPublish.reporter());
});

gulp.task('default', ['build-prod'], function () {
});