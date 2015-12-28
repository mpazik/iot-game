const gulp = require('gulp');
const clean = require('gulp-clean');
const gulpSequence = require('gulp-sequence');
const AwsPublish = require('gulp-awspublish');

const distPath = 'dist';


gulp.task('clean', function () {
    return gulp.src(distPath, {read: false})
        .pipe(clean());
});

gulp.task('copy-public', function () {
    return gulp.src(['public/**/*.*', '!public/dev/**/*.*'])
        .pipe(gulp.dest(distPath));
});

gulp.task('replace-dev-files', function () {

    return gulp.src(['prod/**/*'])
        .pipe(gulp.dest(distPath));
});

gulp.task('build', gulpSequence('clean', 'copy-public', 'replace-dev-files'));

gulp.task('deploy', ['build'],function () {
        process.env['AWS_PROFILE'] = 's3_deploy';

        const publisher = AwsPublish.create({
            region: 'eu-central-1',
            params: {
                Bucket: 'game.dzida-online.pl'
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

gulp.task('default', ['build'], function () {
});